package com.kavya.theledger;

public class Transaction_model {
    int Amount;
    String Date;
    String Description;

    public Transaction_model(int amnt, String Date, String Desc){
        this.Amount = amnt;
        this.Date = Date;
        this.Description = Desc;
    }

    public Transaction_model(){}


    public int getAmount() {
        return this.Amount;
    }

    public String getDescription() {
        return this.Description;
    }

    public String getDate() {
        return this.Date;
    }
    public void setAmount(int amnt) { this.Amount = amnt; }
    public void setDescription(String Desc) { this.Description = Desc; }
    public void setDate(String date) { this.Date = date; }

}
