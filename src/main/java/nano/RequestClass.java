package nano;

import java.time.LocalDateTime;

public class RequestClass {
    private String blockId;

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockDate) {
        this.blockId = blockDate;
    }

    public RequestClass(String blockId) {
        this.blockId = blockId;
    }

    public RequestClass() {
    }
}
