package pridbank.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "utilisateur")
@NamedQueries({
    @NamedQuery(name = "Utilisateur.findAll", query = "SELECT u FROM Utilisateur u"),
    @NamedQuery(name = "Utilisateur.trouverUtilisateurParId", query = "SELECT u FROM Utilisateur u WHERE u.idClient = :idClient"),
    @NamedQuery(name = "Utilisateur.trouverUtilisateurParNom", query = "SELECT u FROM Utilisateur u WHERE u.nomUtilisateur = :nomUtilisateur"),
    @NamedQuery(name = "Utilisateur.findByMotdepasse", query = "SELECT u FROM Utilisateur u WHERE u.motDePasse = :motDePasse"),
    @NamedQuery(name = "Utilisateur.findByDdn", query = "SELECT u FROM Utilisateur u WHERE u.dateDeNaissance = :dateDeNaissance"),
    @NamedQuery(name = "Utilisateur.findByGestionnaire", query = "SELECT u FROM Utilisateur u WHERE u.estUnGestionnaire = :estUnGestionnaire"),
    @NamedQuery(name = "Utilisateur.findClients", query = "SELECT u FROM Utilisateur u WHERE u.estUnGestionnaire = 0"),
    @NamedQuery(name = "Utilisateur.findClientsNonLiesAuCompte", query = "SELECT u FROM Utilisateur u WHERE u.estUnGestionnaire = 0 AND :compte NOT MEMBER OF u.listeDesComptes"),
    @NamedQuery(name = "Utilisateur.findClientsLiesAuCompte", query = "SELECT u FROM Utilisateur u WHERE u.estUnGestionnaire = 0 AND :compte MEMBER OF u.listeDesComptes"),
    @NamedQuery(name = "Utilisateur.compterGestionnaires", query = "SELECT Count(u) FROM Utilisateur u WHERE u.estUnGestionnaire = 1")})
public class Utilisateur implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "IDCLIENT")
    private Integer idClient;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "PSEUDO")
    private String nomUtilisateur;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 256)
    @Column(name = "MOTDEPASSE")
    private String motDePasse;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DDN")
    @Temporal(TemporalType.DATE)
    private Date dateDeNaissance;
    @Basic(optional = false)
    @NotNull
    @Column(name = "GESTIONNAIRE")
    private boolean estUnGestionnaire;
    @JoinTable(name = "proprietaire", joinColumns = {
        @JoinColumn(name = "NUMCLIENT", referencedColumnName = "IDCLIENT")}, inverseJoinColumns = {
        @JoinColumn(name = "NUMCOMPTE", referencedColumnName = "NUMEROCOMPTE")})
    @ManyToMany
    private List<Compte> listeDesComptes;

    public Utilisateur() {
    }

    public Utilisateur(Integer idclient) {
        this.idClient = idclient;
    }
    
    public Utilisateur(Utilisateur utilisateur) {
        this.idClient = utilisateur.getIdClient();
        this.nomUtilisateur = new String(utilisateur.nomUtilisateur);
        this.motDePasse = new String(utilisateur.motDePasse);
        this.dateDeNaissance = utilisateur.dateDeNaissance;
        this.estUnGestionnaire = utilisateur.estUnGestionnaire;
        this.listeDesComptes = utilisateur.getListeDesComptes().stream().collect(Collectors.toList());
    }

    public Utilisateur(Integer idClient, String nomUtilisateur, String motDePasse, Date dateDeNaissance, boolean estUnGestionnaire) {
        this.idClient = idClient;
        this.nomUtilisateur = nomUtilisateur;
        this.motDePasse = motDePasse;
        this.dateDeNaissance = dateDeNaissance;
        this.estUnGestionnaire = estUnGestionnaire;
    }

    public Integer getIdClient() {
        return idClient;
    }

    public void setIdClient(Integer idClient) {
        this.idClient = idClient;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Date getDateDeNaissance() {
        return dateDeNaissance;
    }

    public void setDateDeNaissance(Date dateDeNaissance) {
        this.dateDeNaissance = dateDeNaissance;
    }

    public boolean getEstUnGestionnaire() {
        return estUnGestionnaire;
    }

    public void setEstUnGestionnaire(boolean estUnGestionnaire) {
        this.estUnGestionnaire = estUnGestionnaire;
    }

    public List<Compte> getListeDesComptes() {
        return listeDesComptes;
    }

    public void setListeDesComptes(List<Compte> listeDesComptes) {
        this.listeDesComptes = listeDesComptes;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idClient != null ? idClient.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Utilisateur)) {
            return false;
        }
        Utilisateur other = (Utilisateur) object;
        if ((this.idClient == null && other.idClient != null) || (this.idClient != null && !this.idClient.equals(other.idClient))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pridbank.model.entity.Utilisateur[ idclient=" + idClient + "]";
    }
    
    public String afficherInformations(){
        return idClient + " : " + nomUtilisateur;
    }
    
}
