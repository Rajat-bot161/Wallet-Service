package com.example.service;

import com.example.dto.TransactionCreateRequest;
import com.example.model.Transaction;
import com.example.model.TransactionStatus;
import com.example.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate restTemplate = new RestTemplate();

    private static final String TRANSACTION_CREATED_TOPIC = "transaction_created";

    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction_completed";
    private static final String WALLET_UPDATED_TOPIC = "wallet_updated";

    private static final String WALLET_UPDATE_SUCCESS_STATUS = "SUCCESS";

    private static final String WALLET_UPDATE_FAILED_STATUS = "FAILED";

    public String transact(TransactionCreateRequest request) throws JsonProcessingException {

        Transaction transaction = Transaction.builder()
                .senderId(request.getSender())
                .receiverId(request.getReceiver())
                .externalId(UUID.randomUUID().toString())
                .amount(request.getAmount())
                .transactionStatus(TransactionStatus.PENDING)
                .reason(request.getReason())
                .amount(request.getAmount())
                .build();

        transactionRepository.save(transaction);

        JSONObject obj = new JSONObject();
        obj.put("senderId", transaction.getSenderId());
        obj.put("receiverId", transaction.getReceiverId());
        obj.put("amount", transaction.getAmount());
        obj.put("transactionId",transaction.getExternalId());
        kafkaTemplate.send(TRANSACTION_CREATED_TOPIC, objectMapper.writeValueAsString(obj));

        return transaction.getExternalId();
    }

    @KafkaListener(topics = {WALLET_UPDATED_TOPIC}, groupId = "jbdl123")
    public void updateTransaction(String msg) throws ParseException, JsonProcessingException {

        JSONObject obj = (JSONObject) new JSONParser().parse(msg);
        String externalTransactionId = (String) obj.get("transactionId");
        String receiverPhoneNumber = (String) obj.get("receiverWalletId");
        String senderPhoneNumber = (String) obj.get("senderWalletId");
        String walletUpdateStatus = (String) obj.get("status");
        Long amount = (Long) obj.get("amount");

        TransactionStatus status;
        if(walletUpdateStatus.equals(WALLET_UPDATE_FAILED_STATUS)) {
            status = TransactionStatus.FAILED;
            transactionRepository.updateTransaction(externalTransactionId,status);
        } else {
            status = TransactionStatus.SUCCESSFUL;
            transactionRepository.updateTransaction(externalTransactionId,status);
        }

        JSONObject senderObj = this.restTemplate.getForObject("http://localhost:8081/user/phone/" +senderPhoneNumber, JSONObject.class);
        JSONObject receiverObj = this.restTemplate.getForObject("http://localhost:8081/user/phone/" +receiverPhoneNumber, JSONObject.class);

        String senderEmail = senderObj == null ? null : (String) senderObj.get("email");
        String receiverEmail = receiverObj == null ? null : (String) receiverObj.get("email");

        obj = new JSONObject();
        obj.put("transactionId",externalTransactionId);
        obj.put("transactionStatus",status.toString());
        obj.put("amount", amount);
        obj.put("senderEmail", senderEmail);
        obj.put("receiverEmail", receiverEmail);
        obj.put("senderPhone", senderPhoneNumber);
        obj.put("receiverPhone", receiverPhoneNumber);
        kafkaTemplate.send(TRANSACTION_COMPLETED_TOPIC, objectMapper.writeValueAsString(obj));
    }
}
