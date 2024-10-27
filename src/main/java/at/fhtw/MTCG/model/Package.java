package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Package {
    @JsonAlias({"P_ID"})    //PK
    @JsonProperty("P_ID")
    private Integer P_ID;
    @JsonAlias({"price"})   //costs 5 coins
    @JsonProperty("price")
    private Integer price;
    @JsonAlias({"C1_ID"})   //FK to Card.java
    @JsonProperty("C1_ID")
    private Integer C1_ID;
    @JsonAlias({"C2_ID"})   //FK to Card.java
    @JsonProperty("C2_ID")
    private Integer C2_ID;
    @JsonAlias({"C3_ID"})   //FK to Card.java
    @JsonProperty("C3_ID")
    private Integer C3_ID;
    @JsonAlias({"C4_ID"})   //FK to Card.java
    @JsonProperty("C4_ID")
    private Integer C4_ID;
    @JsonAlias({"C5_ID"})   //FK to Card.java
    @JsonProperty("C5_ID")
    private Integer C5_ID;

    public Package() {}

    public Package(Integer P_ID, Integer price, Integer C1_ID, Integer C2_ID, Integer C3_ID, Integer C4_ID, Integer C5_ID) {
        this.P_ID = P_ID;
        this.price = price;
        this.C1_ID = C1_ID;
        this.C2_ID = C2_ID;
        this.C3_ID = C3_ID;
        this.C4_ID = C4_ID;
        this.C5_ID = C5_ID;
    }

    public Integer get_P_ID() { return P_ID; }
    public void set_P_ID(Integer P_ID) { this.P_ID = P_ID; }

    public Integer get_price() { return price; }
    public void set_price(Integer price) { this.price = price; }

    public Integer get_C1_ID() { return C1_ID; }
    public void set_C1_ID(Integer C1_ID) { this.C1_ID = C1_ID; }

    public Integer get_C2_ID() { return C2_ID; }
    public void set_C2_ID(Integer C2_ID) { this.C2_ID = C2_ID; }

    public Integer get_C3_ID() { return C3_ID; }
    public void set_C3_ID(Integer C3_ID) { this.C3_ID = C3_ID; }

    public Integer get_C4_ID() { return C4_ID; }
    public void set_C4_ID(Integer C4_ID) { this.C4_ID = C4_ID; }

    public Integer get_C5_ID() { return C5_ID; }
    public void set_C5_ID(Integer C5_ID) { this.C5_ID = C5_ID; }
}
