package in.krrishit.services;



import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlockedUserService {
    private ConcurrentHashMap<String, Long> blockedEmails = new ConcurrentHashMap<>();

    public boolean isBlocked(String email) {
        if(blockedEmails.containsKey(email)){
            long expiry = blockedEmails.get(email);
            if(System.currentTimeMillis() < expiry) {
                return true;
            } else {
                blockedEmails.remove(email);
                return false;
            }
        }
        return false;
    }

    public void blockEmail(String email, long blockDurationMillis) {
        blockedEmails.put(email, System.currentTimeMillis() + blockDurationMillis);
    }

    public long getRemainingBlockTime(String email) {
        if(blockedEmails.containsKey(email)){
            long expiry = blockedEmails.get(email);
            long remaining = expiry - System.currentTimeMillis();
            return remaining;
        }
        return 0;
    }
}
