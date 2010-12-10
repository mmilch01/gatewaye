/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.env.util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.nrg.xnat.env.AuthenticatedDevice;

/**
 *
 * @author aditya
 */
public class TestEncryption {
    public static void main (String [] args) {
        try {
            String original = "hello world";
            String encrypted = EncryptionUtils.encrypt(original, new File(AuthenticatedDevice.keyFile));
            String decrypted = EncryptionUtils.decrypt(encrypted, new File(AuthenticatedDevice.keyFile));
            System.out.println(original.equals(decrypted));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TestEncryption.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestEncryption.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(TestEncryption.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(TestEncryption.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(TestEncryption.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(TestEncryption.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(TestEncryption.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
