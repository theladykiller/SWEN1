package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonAlias({"U_ID"})    //PK
    @JsonProperty("U_ID")
    private Integer U_ID;
    @JsonAlias({"username"})
    @JsonProperty("username")
    private String username;
    @JsonAlias({"password"})
    @JsonProperty("password")
    private String password;
    @JsonAlias({"coins"})   //start at 20 coins
    @JsonProperty("coins")
    private Integer coins;
    @JsonAlias({"score"})   //start at 100 score
    @JsonProperty("score")
    private Integer score;
    @JsonAlias({"elo"})     //Iron/Silver/Gold/Platinum/Diamond/Master/Grandmaster/Challenger
    @JsonProperty("elo")
    private String elo;
    @JsonAlias({"game_count"})
    @JsonProperty("game_count")
    private Integer game_count;
    @JsonAlias({"D_ID"})    //FK to Deck.java
    @JsonProperty("D_ID")
    private Integer D_ID;

    public User() {}

    public User(Integer U_ID, String username, String password, Integer coins, Integer score, String elo, Integer game_count, Integer D_ID) {
        this.U_ID = U_ID;
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.score = score;
        this.elo = elo;
        this.game_count = game_count;
        this.D_ID = D_ID;
    }

    public Integer get_U_ID() {
        return U_ID;
    }
    public void set_U_ID(Integer U_ID) {
        this.U_ID = U_ID;
    }

    public String get_username() {
        return username;
    }
    public void set_username(String username) {
        this.username = username;
    }

    public String get_password() { return password; }
    public void set_password(String password) {
        this.password = password;
    }

    public Integer get_coins() {
        return coins;
    }
    public void set_coins(Integer coins) {
        this.coins = coins;
    }

    public Integer get_score() { return score; }
    public void set_score(Integer score) { this.score = score; }

    public String get_elo() { return elo; }
    public void set_elo(String elo) { this.elo = elo; }

    public Integer get_game_count() { return game_count; }
    public void set_game_count(Integer game_count) { this.game_count = game_count; }

    public Integer get_D_ID() {
        return D_ID;
    }
    public void set_D_ID(Integer D_ID) {
        this.D_ID = D_ID;
    }
}
