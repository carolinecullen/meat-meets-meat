package Models;

public class User {

    private String username;
    private String password;
    private String first;
    private String last;
    private Integer cid;

    public User(String username, String password, String first, String last, Integer cid) {
        this.username = username;
        this.password = password;
        this.first = first;
        this.last = last;
        this.cid = cid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }
}
