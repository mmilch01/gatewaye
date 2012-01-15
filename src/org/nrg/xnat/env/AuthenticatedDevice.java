/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nrg.xnat.env;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.nrg.xnat.env.util.EncryptionUtils;
import org.nrg.xnat.util.Utils;

/**
 *
 * @author Aditya Siram
 */
public class AuthenticatedDevice {

    private String username;
    private String password;
    private String group_name;
    private String name; 
    public static String keyFile = System.getProperty("user.home")+"/.xnatgateway/password.key";

    AuthenticatedDevice(String name, String group_name) throws NoSuchAlgorithmException, IOException {
        this.name = name;
        this.group_name = group_name;
        EncryptionUtils.createKeyFile(new File(AuthenticatedDevice.keyFile));
    }

    String getUsername() {
        return this.username;
    }

    String getPassword() {
        return this.password;
    }

    boolean hasUsername() {
        return this.username != null;
    }

    boolean hasPassword() {
        return this.password != null;
    }

    boolean isValid() {
        return Utils.has_content(getUsername())
                && Utils.has_content(getPassword());
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setPassword(String password) {
        this.password = password;
    }

    Vector read_from_properties(Properties p) throws
            NoSuchAlgorithmException,
            IOException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {
        boolean correct_properties = true;
        String _username = p.getProperty(group_name + "." + this.name + ".User");
        String _password = p.getProperty(group_name + "." + this.name + ".Pass");

        Vector warnings = new Vector();
        if (!Utils.has_content(_username)) {
            warnings.add(" no username specified");
            correct_properties = false;
        }
        if (!Utils.has_content(_password)) {
            warnings.add("no password specified");
            correct_properties = false;
        }
        if (correct_properties) {
            setUsername(_username.trim());
            String password = EncryptionUtils.decrypt(_password, new File(AuthenticatedDevice.keyFile));
            setPassword(password);
        }
        return warnings;
    }

    Properties add_to_properties(Properties p) throws
            NoSuchAlgorithmException,
            IOException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {
        p.setProperty(this.group_name + "." + this.name + ".Pass", EncryptionUtils.encrypt(getPassword(), new File(AuthenticatedDevice.keyFile)));
        p.setProperty(this.group_name + "." + this.name + ".User", getUsername());
        return p;
    }

    Properties remove_from_properties(Properties p) {
        p.remove(this.group_name + "." + this.name + ".User");
        p.remove(this.group_name + "." + this.name + ".Pass");
        return p;
    }

    @Override
    public String toString() {
        return "Username : " + getUsername() + "\n"
                + "Password : " + getPassword() + "\n";
    }
}
