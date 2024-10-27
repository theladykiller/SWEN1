package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Deck {
    @JsonAlias({"D_ID"})    //PK
    @JsonProperty("D_ID")
    private Integer D_ID;
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

    public Deck () {}

    public Deck ( Integer D_ID, Integer C1_ID, Integer C2_ID, Integer C3_ID, Integer C4_ID) {
        this.D_ID = D_ID;
        this.C1_ID = C1_ID;
        this.C2_ID = C2_ID;
        this.C3_ID = C3_ID;
        this.C4_ID = C4_ID;
    }

    public Integer get_D_ID() { return D_ID; }
    public void set_D_ID(Integer D_ID) { this.D_ID = D_ID; }

    public Integer get_C1_ID() { return C1_ID; }
    public void set_C1_ID(Integer C1_ID) { this.C1_ID = C1_ID; }

    public Integer get_C2_ID() { return C2_ID; }
    public void set_C2_ID(Integer C2_ID) { this.C2_ID = C2_ID; }

    public Integer get_C3_ID() { return C3_ID; }
    public void set_C3_ID(Integer C3_ID) { this.C3_ID = C3_ID; }

    public Integer get_C4_ID() { return C4_ID; }
    public void set_C4_ID(Integer C4_ID) { this.C4_ID = C4_ID; }
}
