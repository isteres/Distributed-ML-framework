package Framework.Domain;

import java.io.Serializable;

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