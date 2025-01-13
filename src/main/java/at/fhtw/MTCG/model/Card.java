package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {
    @JsonAlias({"C_ID"})    //PK
    @JsonProperty("Id")                 //because of curl script
    private String C_ID;
    @JsonAlias({"name"})
    @JsonProperty("Name")               //because of curl script
    private String name;
    @JsonAlias({"damage"})
    @JsonProperty("Damage")             //because of curl script
    private Integer damage;
    @JsonAlias({"element_type"})    //Water/Fire/Normal
    @JsonProperty("element_type")
    private String element_type;
    @JsonAlias({"card_type"})   //Monster/Magic
    @JsonProperty("card_type")
    private String card_type;
    @JsonAlias({"trait"})   //Goblin/Dragon/Wizard/Ork/Knight/Kraken/Elf
    @JsonProperty("trait")
    private String trait;
    @JsonAlias({"U_ID"})    //FK to User.java
    @JsonProperty("U_ID")
    private Integer U_ID;

    public Card() {}

    public Card(String C_ID, String name, Integer damage, String element_type, String card_type, String trait, Integer U_ID) {
        this.C_ID = C_ID;
        this.name = name;
        this.damage = damage;
        this.element_type = element_type;
        this.card_type = card_type;
        this.trait = trait;
        this.U_ID = U_ID;
    }

    public String get_C_ID() { return C_ID; }
    public void set_C_ID(String C_ID) { this.C_ID = C_ID; }

    public String get_name() { return name; }
    public void set_name(String name) { this.name = name; }

    public Integer get_damage() { return damage; }
    public void set_damage(Integer damage) { this.damage = damage; }

    public String get_element_type() { return element_type; }
    public void set_element_type(String element_type) { this.element_type = element_type; }

    public String get_card_type() { return card_type; }
    public void set_card_type(String card_type) { this.card_type = card_type; }

    public String get_trait() { return trait; }
    public void set_trait(String trait) { this.trait = trait; }

    public Integer get_U_ID() { return U_ID; }
    public void set_U_ID(Integer U_ID) { this.U_ID = U_ID; }
}
