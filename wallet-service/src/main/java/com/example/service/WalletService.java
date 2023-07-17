package com.example.service;

import com.example.model.Wallet;
import com.example.repository.WalletRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Value("${wallet.initial.balance}")
    Long balance;
    @Autowired
    WalletRepository walletRepository;
    private static final String USER_CREATED_TOPIC = "user_created";
    private static final String TRANSACTION_CREATED_TOPIC = "transaction_created";

    private static final String WALLET_UPDATED_TOPIC = "wallet_updated";

    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;
    //user onboarding flow
    @KafkaListener(topics= {USER_CREATED_TOPIC}, groupId = "jbdl123")
    public void createWallet(String msg) throws ParseException {
        JSONObject obj = (JSONObject) new JSONParser().parse(msg);

        String walletId = (String) obj.get("phone");

        Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .currency("INR")
                .balance(balance)
                .build();
        walletRepository.save(wallet);
    }

    //user transaction flow

    @KafkaListener(topics= {TRANSACTION_CREATED_TOPIC}, groupId = "jbdl123")
    public void updateWallet(String msg) throws ParseException, JsonProcessingException {

        JSONObject obj = (JSONObject) new JSONParser().parse(msg);

        String receiverWalletId = (String) obj.get("receiverId");
        String senderWalletId = (String) obj.get("senderId");
        Long amount = (Long) obj.get("amount");
        String transactionId = (String) obj.get("transactionId");

        try {
            Wallet senderWallet = walletRepository.findByWalletId(senderWalletId);
            Wallet receiverWallet = walletRepository.findByWalletId(receiverWalletId);

            if(senderWallet == null || receiverWallet == null || senderWallet.getBalance()<amount) {

                obj = this.init(receiverWalletId, senderWalletId,amount,transactionId,"FAILED");
                obj.put("senderWalletBalance", senderWallet == null ? 0 : senderWallet.getBalance());
                kafkaTemplate.send(WALLET_UPDATED_TOPIC, objectMapper.writeValueAsString(obj));
                return;
            }

            //        walletRepository.decrementWallet(senderWalletId,amount);
            //        walletRepository.incrementWallet(receiverWalletId,amount);

            walletRepository.updateWallet(senderWalletId, -amount);
            walletRepository.updateWallet(receiverWalletId,amount);

            obj = this.init(receiverWalletId, senderWalletId,amount,transactionId,"SUCCESS");

            kafkaTemplate.send(WALLET_UPDATED_TOPIC, objectMapper.writeValueAsString(obj));


        } catch (Exception ex) {
            obj = this.init(receiverWalletId, senderWalletId,amount,transactionId,"FAILED");
            obj.put("errorMsg", ex.getMessage());
            kafkaTemplate.send(WALLET_UPDATED_TOPIC, objectMapper.writeValueAsString(obj));
        }
    }

    //Publish event of wallet creation

    private JSONObject init(String receiverWalletId, String senderWalletId, Long amount,
                            String transactionId, String status) {
        JSONObject obj = new JSONObject();
        obj.put("transactionId", transactionId);
        obj.put("senderWalletId", senderWalletId);
        obj.put("receiverWalletId", receiverWalletId);
        obj.put("amount", amount);
        obj.put("status", status);

        return obj;
    }
}
