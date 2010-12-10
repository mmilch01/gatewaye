package org.nrg.xnat.env;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Priority;
import org.nrg.xnat.env.GatewayEnvironment.Log;
import org.nrg.xnat.env.GatewayEnvironment.Message;
import org.nrg.xnat.gateway.Tools;
import org.nrg.xnat.util.Utils;

/**
 * All static functions that write the current properties to disk.
 * @author Aditya Siram
 */

class Writer {

    /**
     * Write the given properties to the given file. The file is overwritten if it exists.
     * @param p
     * @param f
     * @throws IOException
     */
    static void write_properties(Properties p, File f) throws IOException {
        write_properties(p,f,"Auto-generated, please do not edit");
    }

    /**
     * Copy source file to destination file, and add the header string to the top
     * of the destination file.
     * @param src
     * @param dst
     * @param header
     * @throws IOException
     */
    static void copy_with_header (File src, File dst, String header) throws IOException {
        List read_lines = FileUtils.readLines(src);
        read_lines.add(0,header);
        FileUtils.writeLines(dst, read_lines);
    }

    /**
     * Store the given properties with the header comment.
     * @param p
     * @param f
     * @param header comment which appears on top of the properties file
     * @throws IOException
     */
    static void write_properties (Properties p, File f, String header) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        boolean successful = false;
        try {
            p.store(fos, header);
            successful = true;
        }
        finally {
            try {fos.close();} catch (IOException e) {
                if (successful) throw e;
                else System.out.println("Could not update the properties file");
            }
        }
    }
    
    /**
     * If any warnings were generated when validating the properties 
     * copy the properties file to a back-up location and tack the warnings
     * to the top.
     * 
     * @param f
     * @param p
     * @param log
     * @throws IOException
     */
    static void backup_props_if_necessary (File f, Properties p, Log log) throws IOException {
        Vector messages = log.getMessages();
        if (messages.size() > 0) {
            String header = "This backup file was created because of the following errors in the original: "
                            + System.getProperty("line.separator")
                            + log.toString();
            header = Utils.prefix_lines(header, "# ");
            Writer.copy_with_header(f, new File("gateway.properties.bak"), header);
            for (int i = 0; i < messages.size(); i++) {
                Tools.LogMessage(Priority.WARN_INT, ((Message) messages.get(i)).toString());
            }
        }
    }
}
