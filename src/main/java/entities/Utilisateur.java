package entities;

import enumeration.UtilisateurSexeEnum;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "utilisateur")
@NamedQueries
        ({
                @NamedQuery(name = "Utilisateur.findAll", query = "SELECT u FROM Utilisateur u"),
                @NamedQuery(name = "Utilisateur.findOne", query = "SELECT u FROM Utilisateur u WHERE  u.nom=:nom AND u.prenom=:prenom AND u.courriel=:courriel AND u.sexe=:sexe"),
                @NamedQuery(name = "Utilisateur.findAllUtil",query = "SELECT DISTINCT  u FROM Utilisateur u , UtilisateurRole ur WHERE u.id=ur.utilisateurIdUtilisateur.id and ur.roleIdRole.id <> 4"),
                @NamedQuery(name = "Utilisateur.findActiv", query = "SELECT u FROM Utilisateur u , UtilisateurRole ur WHERE u.actif=TRUE AND u.numMembre IS NULL"),
                @NamedQuery(name = "Utilisateur.findInactiv", query = "SELECT u FROM Utilisateur u , UtilisateurRole ur WHERE u.actif=FALSE AND u.numMembre IS NULL"),
                @NamedQuery(name=  "Utilisateur.searchName", query="SELECT u FROM Utilisateur u , UtilisateurRole ur WHERE u.nom=:nom AND u.id=ur.utilisateurIdUtilisateur.id AND ur.roleIdRole.id=4"),
                @NamedQuery(name = "Utilisateur.findLastMembre", query = "SELECT u FROM Utilisateur u , UtilisateurRole ur WHERE u.id=ur.utilisateurIdUtilisateur.id AND ur.roleIdRole.id=4 ORDER BY u.numMembre DESC"),
                @NamedQuery(name = "Utilisateur.searchMembre", query = "SELECT u FROM Utilisateur u WHERE u.numMembre=:numMembre"),
                @NamedQuery(name = "Utilisateur.findByLogin", query = "SELECT u FROM Utilisateur u WHERE u.login=:login"),
                @NamedQuery(name = "Utilisateur.findByLoginMail", query = "SELECT u FROM Utilisateur u WHERE u.login=:login AND u.courriel=:courriel"),
                @NamedQuery(name = "Utilisateur.findAllCli", query = "SELECT u FROM Utilisateur u , UtilisateurRole ur WHERE u.id=ur.utilisateurIdUtilisateur.id AND ur.roleIdRole.id=4"),
                @NamedQuery(name = "Utilisateur.findCliActiv", query = "SELECT u FROM Utilisateur u , UtilisateurRole ur WHERE u.actif=TRUE AND u.id=ur.utilisateurIdUtilisateur.id AND ur.roleIdRole.id=4"),
                @NamedQuery(name = "Utilisateur.findCliInactiv", query = "SELECT u FROM Utilisateur u , UtilisateurRole ur WHERE u.actif=FALSE AND u.id=ur.utilisateurIdUtilisateur.id AND ur.roleIdRole.id=4"),
        })
public class Utilisateur implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdUtilisateur", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 150)
    @NotNull
    @Column(name = "Nom", nullable = false, length = 150)
    private String nom;

    @Size(max = 150)
    @NotNull
    @Column(name = "Prenom", nullable = false, length = 150)
    private String prenom;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "Sexe", nullable = false)
    private UtilisateurSexeEnum sexe;

    @Size(max = 255)
    @Column(name = "Courriel")
    private String courriel;

    @Size(max = 255)
    @Column(name = "Login")
    private String login;

    @Size(max = 255)
    @Column(name = "Mdp")
    private String mdp;

    @NotNull
    @Column(name = "Actif", nullable = false)
    private Boolean actif = true;

    @Size(max = 10)
    @Column(name = "NumMembre", length = 10)
    private String numMembre;

    @OneToMany(mappedBy = "utilisateurIdUtilisateur")
    private Collection<Facture> factures;

    @OneToMany(mappedBy = "utilisateurIdUtilisateur")
    private Collection<UtilisateurAdresse> utilisateurAdresse;

    @OneToMany(mappedBy = "utilisateurIdUtilisateur")
    private Collection<UtilisateurMagasin> utilisateurMagasin;

    @OneToMany(mappedBy = "utilisateurIdUtilisateur")
    private Collection<UtilisateurRole> utilisateurRole;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public UtilisateurSexeEnum getSexe() {
        return sexe;
    }

    public void setSexe(UtilisateurSexeEnum sexe) {
        this.sexe = sexe;
    }

    public String getCourriel() {
        return courriel;
    }

    public void setCourriel(String courriel) {
        this.courriel = courriel;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public String getNumMembre() {
        return numMembre;
    }

    public void setNumMembre(String numMembre) {
        this.numMembre = numMembre;
    }

    public Collection<Facture> getFactures() {
        return factures;
    }

    public void setFactures(Collection<Facture> factures) {
        this.factures = factures;
    }

    public Collection<UtilisateurAdresse> getUtilisateurAdresse() {
        return utilisateurAdresse;
    }

    public void setUtilisateurAdresse(Collection<UtilisateurAdresse> utilisateurAdresse) {
        this.utilisateurAdresse = utilisateurAdresse;
    }

    public Collection<UtilisateurMagasin> getUtilisateurMagasin() {
        return utilisateurMagasin;
    }

    public void setUtilisateurMagasin(Collection<UtilisateurMagasin> utilisateurMagasin){
        this.utilisateurMagasin = utilisateurMagasin;
    }
    public Collection<UtilisateurRole> getUtilisateurRole() {
        return utilisateurRole;
    }
    public void setUtilisateurRole(Collection<UtilisateurRole> utilisateurRole) {
        this.utilisateurRole = utilisateurRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur that = (Utilisateur) o;
        return Objects.equals(id, that.id) && Objects.equals(nom, that.nom) && Objects.equals(prenom, that.prenom) && sexe == that.sexe && Objects.equals(courriel, that.courriel) && Objects.equals(login, that.login) && Objects.equals(mdp, that.mdp) && Objects.equals(actif, that.actif) && Objects.equals(numMembre, that.numMembre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, sexe, courriel, login, mdp, actif, numMembre);
    }
}