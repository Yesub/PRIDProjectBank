package pridbank.model.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "compte")
@NamedQueries({
    @NamedQuery(name = "Compte.findAll", query = "SELECT c FROM Compte c"),
    @NamedQuery(name = "Compte.trouverCompteParNumero", query = "SELECT c FROM Compte c WHERE c.numero = :numeroCompte"),
    @NamedQuery(name = "Compte.findByTypecompte", query = "SELECT c FROM Compte c WHERE c.type = :typeCompte"),
    @NamedQuery(name = "Compte.findBySolde", query = "SELECT c FROM Compte c WHERE c.solde = :solde"),
    @NamedQuery(name = "Compte.findByBloque", query = "SELECT c FROM Compte c WHERE c.estBloque = :bloque"),
    @NamedQuery(name = "Compte.findComptesNonLiesAuClient", query = "SELECT c FROM Compte c WHERE :utilisateur NOT MEMBER OF c.listeProprietaires"),
    @NamedQuery(name = "Compte.findComptesLiesAuClient", query = "SELECT c FROM Compte c WHERE :utilisateur MEMBER OF c.listeProprietaires")})
public class Compte implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "NUMEROCOMPTE")
    private Integer numero;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TYPECOMPTE")
    private Character type;
    @Basic(optional = false)
    @NotNull
    @Column(name = "SOLDE")
    private float solde;
    @Basic(optional = false)
    @NotNull
    @Column(name = "BLOQUE")
    private boolean estBloque;
    @JoinTable(name = "proprietaire", joinColumns = {
        @JoinColumn(name = "NUMCOMPTE", referencedColumnName = "NUMEROCOMPTE")}, inverseJoinColumns = {
        @JoinColumn(name = "NUMCLIENT", referencedColumnName = "IDCLIENT")})
    @ManyToMany
    private List<Utilisateur> listeProprietaires;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "debiteur")
    private List<Virement> virementsSortants; // juste ?
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "credite")
    private List<Virement> virementsEntrants; // juste ?
    @Transient
    private static Map<Character, Float> plancher = new HashMap<>();
    
    @PostConstruct
    public void init(){
//        Character g = 'G';
//        Character n = 'N';
//        Character r = 'R';
        plancher.put('G', -5000.0f);
        plancher.put('N', -1000.0f);
        plancher.put('R', 0.0f);
    }
    
    public Compte() {
        this.init();
    }

    public Compte(Integer numero) {
        this.numero = numero;
    }
    
    public Compte(Compte compte) {
        this.numero = compte.numero;
        this.type = compte.getType();
        this.solde = compte.getSolde();
        this.estBloque = compte.getEstBloque();
        this.listeProprietaires = compte.getListeProprietaires().stream().collect(Collectors.toList());
    }
    
    public Compte(Integer numero, Character type, float solde, boolean estBloque) {
        this.numero = numero;
        this.type = type;
        this.solde = solde;
        this.estBloque = estBloque;
    }
    
    public static Map<Character, Float> getPlancher() {
        return plancher;
    }

    public static void setPlancher(Map<Character, Float> plancher) {
        Compte.plancher = plancher;
    }
    
    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Character getType() {
        return type;
    }

    public void setType(Character type) {
        this.type = type;
    }
    
    public float getSolde() {
        return solde;
    }

    public void setSolde(float solde) {
        this.solde = solde;
    }

    public boolean getEstBloque() {
        return estBloque;
    }

    public void setEstBloque(boolean estBloque) {
        this.estBloque = estBloque;
    }

    public List<Utilisateur> getListeProprietaires() {
        return listeProprietaires;
    }

    public void setListeProprietaires(List<Utilisateur> listeProprietaires) {
        this.listeProprietaires = listeProprietaires;
    }

    public List<Virement> getVirementsSortants() {
        return virementsSortants;
    }

    public void setVirementsSortants(List<Virement> virementsSortants) {
        this.virementsSortants = virementsSortants;
    }

    public List<Virement> getVirementsEntrants() {
        return virementsEntrants;
    }

    public void setVirementsEntrants(List<Virement> virementsEntrants) {
        this.virementsEntrants = virementsEntrants;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (numero != null ? numero.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Compte)) {
            return false;
        }
        Compte other = (Compte) object;
        if ((this.numero == null && other.numero != null) || (this.numero != null && !this.numero.equals(other.numero))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pridbank.model.entity.Compte[ numerocompte=" + numero + " ]";
    }
    
    public String afficherInformations(){
        return numero + " : " + String.format(Locale.FRANCE,"%.2f", solde) + " €" + " : " + getType() + (getEstBloque() ? " : Compte bloqué" : "");
    }
}
