package nano;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class DataBlock {
    private int id;
    private String block_id;
    private String timestamp;
    private String type;
    private String link;
    private String link_as_account;
    private String account;
    private String amount;

    public DataBlock(int id, String block_id, String timestamp, String type, String link, String link_as_account, String account, String amount) {
        this.id = id;
        this.block_id = block_id;
        this.timestamp = timestamp;
        this.type = type;
        this.link = link;
        this.link_as_account = link_as_account;
        this.account = account;
        this.amount = amount;
    }

    public DataBlock() {}

    public void setValue(String fieldName, Object object) {
        Method method = null;

        if (object == null) {
            System.out.println("setValueP: Passed object is null");
        } else if (object.getClass() != String.class && object.getClass() != Integer.class) {
            System.out.println("setValueP: assed object is not a String and Integer but a " + object.getClass());
        } else {
            try {
                method = this.getClass().getDeclaredMethod("set" + Character.toString(fieldName.charAt(0)).toUpperCase()+fieldName.substring(1),object.getClass());
                //method = this.getClass().getMethod("set" + Character.toString(fieldName.charAt(0)).toUpperCase()+fieldName.substring(1));
            } catch (NoSuchMethodException | NullPointerException e) {
                System.out.println("Tried to create method: " + "set" + Character.toString(fieldName.charAt(0)).toUpperCase()+fieldName.substring(1));
                e.printStackTrace();
            }

            try {
                if (method != null) {
                    method.invoke(this,object);
                } else
                    System.out.println("Object method is a NULL!");
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.out.println("Tried to access method: " + "set" + Character.toString(fieldName.charAt(0)).toUpperCase()+fieldName.substring(1) + " with object type: " + object.getClass().toString());
                e.printStackTrace();
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBlock_id() {
        return block_id;
    }

    public void setBlock_id(String block_id) {
        this.block_id = block_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink_as_account() {
        return link_as_account;
    }

    public void setLink_as_account(String link_as_account) {
        this.link_as_account = link_as_account;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}