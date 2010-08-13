/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.env;

import java.io.IOException;

/**
 * The functions in this class is used only once to initialize the environment on startup.
 * Its main purpose is to parse the properties file ensuring that it makes
 * sense.
 *
 * It tries as much as possible to keep going in the face of errors. Devices on which
 * these errors occured are ignored and a warnings are logged.
 *
 * @author Aditya Siram
 */

interface PopulateStrategyInterface {
    void execute(InternalNetworkDevices ds) throws IOException;
}
