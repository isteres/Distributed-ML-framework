package Framework.Domain;

import java.io.Serializable;


/**
 * Represents a chunk of a machine learning model file for streaming transmission.
 * 
 * Used by {@link Framework.Server.ModelSenderThread} and {@link Framework.Client.ModelDownloaderThread}
 * to transfer large model files in smaller, manageable pieces over the network. The main goal of 
 * this class is to be able to read the whole model file in parallel into chunks and send them afterwards. 
 * With no need to send it in order, a {@link java.io.RandomAccessFile} will write the chunks to disk using the
 * correct position information contained in each chunk.
 * 
 */
public class ModelChunk implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private long startPosition;
    private byte[] data;

    public ModelChunk(int chunkIndex, long startPosition, byte[] data) {
        this.startPosition = startPosition;
        this.data = data;
    }

    public long getStartPosition() { return startPosition; }
    public byte[] getData() { return data; }
}