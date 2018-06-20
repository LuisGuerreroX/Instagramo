package com.altice.hojuelita.instagramo;


public class Noticia {

    private String Description;
    private String Location;
    private String URL;

    public Noticia() {

    }

    public Noticia(String Description, String Location, String URL) {
        this.Description = Description;
        this.Location = Location;
        this.URL = URL;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String Location) {
        this.Location = Location;
    }

    public String getUrl() {
        return URL;
    }

    public void setUrl(String URL) {
        this.URL = URL;
    }

}
