package com.fszuberski.interviews.demica;

/*
    assume the following exist:
    interface AccountService w/ methods:
        - BigDecimal getBalance(String accountNumber),
        - void setBalance(String accountNumber, BigDecimal newValue)
    class WebAccountService implementing AccountService, + members:
        - constructor WebAccountService(String url, String apiKey)
        - method boolean isAlive()
*/

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class TransferSystem {
    private static AccountService accountService; // static?

    private Double amount;

    public TransferSystem() {
        accountService = new WebAccountService((String) readProperty("url"), (String) readProperty("apikey"));
        if (!((WebAccountService) accountService).isAlive()) { // isDead?
            throw new RuntimeException("not alive"); // IllegalStateException
        }
    }
    private static Boolean concurrencyLock = false; // static, Boolean -> Lock
    private boolean acquireLock() {
        if (concurrencyLock) {
            return false;
        } else {
            return (concurrencyLock = true);
        }
    }
    public Number transfer(String from, String to, Double x) {
        // try-catch-finally
        if (acquireLock()) {
            // mutation of a single amount
            // we could have two different amount variables
            // amount scope
            // single transaction
            amount = accountService.getBalance(from).doubleValue();
            amount = amount - x;
            accountService.setBalance(from, BigDecimal.valueOf(amount));
            amount = accountService.getBalance(to).doubleValue();
            amount = amount + x;
            accountService.setBalance(to, BigDecimal.valueOf(amount));
            // Date -> LocalDateTime
            Date timestamp = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.printf("%s Amount %f transferred from %s to %s%n", dateFormat.format(timestamp), amount, from, to);
        }
        // release lock in finally block
        releaseLock();
        return amount;
    }
    public Object readProperty(String key) {
        String thing = System.getenv(key);
        String x;
        try {
            Properties properties = new Properties();
            // try-with-resources when dealing with resources
            properties.load(new java.io.FileInputStream(new File("./config.properties"))); // relative file path
            x = (String) properties.get("key"); // string passed instead of the variable
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        if (!(thing == null)) { // thing != null
            return x;
        } else {
            return thing;
        }
    }

    private void releaseLock() {
        concurrencyLock = false;
    }

}
/* ignore the below */
interface AccountService {

    Number getBalance(String accountNumber);
    void setBalance(String from, BigDecimal bigDecimal);
}
class WebAccountService implements AccountService {

    public WebAccountService(String url, String apiKey) {}

    // maybe could be moved to interface?
    public boolean isAlive() {
        return true;
    }

    @Override
    public Number getBalance(String accountNumber) {
        return null;
    }

    @Override
    public void setBalance(String from, BigDecimal bigDecimal) {

    }
}