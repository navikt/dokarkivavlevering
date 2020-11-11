package no.nav.dokarkivavlevering.sftp;

public class AvleveringSFTPRoute {

    public static final String SFTP_ENDPOINT =
            "sftp://{{sftp.url}}:{{sftp.port}}/{{sftp.remoteFilePath}}" +
                    "?username={{sftp.username}}" +
                    "&password=" +
                    "&binary=true" +
                    "&jschLoggingLevel=TRACE" +
                    "&privateKeyFile={{sftp.privateKeyFile}}" +
                    "&privateKeyPassphrase={{sftp.privateKeyPassphrase}}" +
                    "&preferredAuthentications=publickey";
}
