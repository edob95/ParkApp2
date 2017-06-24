package com.example.edoardo.parkapp;

/**
 * Created by Niko on 20/06/2017.
 */

public class Park {

    private long park_id;
    private String indirizzo;
    private String park_type;
    private String date;
    private String ora_inizio;
    private String ora_fine;


    public static final String TRUE = "1";
    public static final String FALSE = "0";

    public Park(){
        indirizzo="";
        park_type="";
        date="";
        ora_inizio="";
        ora_fine="";

    }
    public Park(String indirizzo, String park_type, String date, String ora_inizio, String ora_fine){
        this.indirizzo=indirizzo;
        this.park_type=park_type;
        this.date=date;
        this.ora_inizio=ora_inizio;
        this.ora_fine=ora_fine;

    }
    public Park(String indirizzo, int park_id, String park_type, String date, String ora_inizio, String ora_fine){
        this.park_id=park_id;
        this.indirizzo=indirizzo;
        this.park_type=park_type;
        this.date=date;
        this.ora_inizio=ora_inizio;
        this.ora_fine=ora_fine;

    }
    public long getPark_id(){
        return park_id;
    }
    public void setPark_id(long park_id){
        this.park_id = park_id;
    }
    public String getIndirizzo(){ return indirizzo;}
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo;}
    public String getPark_type(){
        return park_type;
    }
    public void setPark_type(String park_type){
        this.park_type=park_type;
    }
    public String getDate(){
        return date;
    }
    public void setDate(String date){
        this.date=date;
    }
    public String getOra_inizio(){
        return ora_inizio;
    }
    public void setOra_inizio(String ora_inizio){
        this.ora_inizio=ora_inizio;
    }
    public String getOra_fine(){
        return ora_fine;
    }
    public void setOra_fine(String ora_fine){
        this.ora_fine=ora_fine;
    }

}
