package org.example.tp2;

public class Person {
    private int id;
    private String nom;
    private String prenom;

    public Person(int id, String nom, String prenom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
}
