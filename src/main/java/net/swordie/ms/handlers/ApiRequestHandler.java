package net.swordie.ms.handlers;

import net.swordie.ms.Server;
import net.swordie.ms.ServerConstants;
import net.swordie.ms.client.Client;
import net.swordie.ms.client.User;
import net.swordie.ms.connection.InPacket;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.ms.connection.packet.ApiResponse;
import net.swordie.ms.constants.GameConstants;
import net.swordie.ms.enums.AccountCreateResult;
import net.swordie.ms.enums.AccountType;
import net.swordie.ms.enums.ApiTokenResultType;
import net.swordie.ms.client.Account;
import net.swordie.ms.util.Util;
import org.apache.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * @author Sjonnie
 * Created on 10/5/2018.
 */
public class ApiRequestHandler {

    private static final Logger log = Logger.getLogger(ApiRequestHandler.class);
    private static final int TOKEN_LENGTH = 50;

    public static void handleTokenRequest(Client c, InPacket inPacket) {
        String name = inPacket.decodeString();
        String password = inPacket.decodeString();
        User user = User.getFromDBByName(name);
        ApiTokenResultType atrt;
        boolean success = false;
        if (user == null) {
            atrt = ApiTokenResultType.InvalidUserPassCombination;
        } else {
            String dbPassword = user.getPassword();
            boolean hashed = Util.isStringBCrypt(dbPassword);
            if (hashed) {
                try {
                    success = BCrypt.checkpw(password, dbPassword);
                } catch (IllegalArgumentException e) { // if password hashing went wrong
                    log.error(String.format("bcrypt check in login has failed! dbPassword: %s; stack trace: %s", dbPassword, e.getStackTrace().toString()));
                    success = false;
                }
            } else {
                success = password.equals(dbPassword);
            }
        }
        String tokenStr = "";
        if (success) {
            atrt = ApiTokenResultType.Success;
            // Generate token with cryptographically secure random bytes
            byte[] tokenBytes = new byte[TOKEN_LENGTH];
            new SecureRandom().nextBytes(tokenBytes);
            tokenStr = Base64.getEncoder().encodeToString(tokenBytes);
            Server.getInstance().addAuthToken(tokenStr.getBytes(), user.getId());
        } else {
            atrt = ApiTokenResultType.InvalidUserPassCombination;
        }
        c.write(ApiResponse.tokenRequestResult(atrt, tokenStr));
    }

    public static void handleCreateAccountRequest(Client c, InPacket inPacket) {
        String name = inPacket.decodeString();
        String pwd = inPacket.decodeString();
        String email = inPacket.decodeString();
        AccountCreateResult acr = AccountCreateResult.Success;
        if (User.getFromDBByName(name) != null) {
            acr = AccountCreateResult.NameInUse;
        } else if (Account.getFromDBByIp(c.getIP()) != null) {
            acr = AccountCreateResult.IpAlreadyHasAccount;
        } else if (name.length() < 4 || pwd.length() < 6) {
            acr = AccountCreateResult.Unknown;
        }
        if (acr == AccountCreateResult.Success) {
            User user = new User(name);
            user.setHashedPassword(pwd);
            user.setEmail(email);
            user.setRegisterIp(c.getIP());
            user.setCharacterSlots(ServerConstants.MAX_CHARACTERS / 2);
            DatabaseManager.saveToDB(user);
        }
        c.write(ApiResponse.createAccountResult(acr));
    }
}
