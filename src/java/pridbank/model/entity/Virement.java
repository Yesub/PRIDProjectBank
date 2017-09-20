package pridbank.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "virement")
@NamedQueries({
    @NamedQuery(name = "Virement.findAll", query = "SELECT v FROM Virement v"),
    @NamedQuery(name = "Virement.findByNumerovirement", query = "SELECT v FROM Virement v WHERE v.numerovirement = :numerovirement"),
    @NamedQuery(name = "Virement.findByMontant", query = "SELECT v FROM Virement v WHERE v.montant = :montant"),
    @NamedQuery(name = "Virement.findByDatevirement", query = "SELECT v FROM Virement v WHERE v.datevirement = :datevirement"),
    @NamedQuery(name = "Virement.findByDateexecution", query = "SELECT v FROM Virement v WHERE v.dateexecution = :dateexecution"),
    @NamedQuery(name = "Virement.trouverVirementsAExecuter", query = "SELECT v FROM Virement v WHERE v.dateexecution = null AND v.datevirement <= :datevirement ORDER BY v.datevirement")})
public class Virement implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "NUMEROVIREMENT")
    private Integer numerovirement;
    @Basic(optional = false)
    @NotNull
    @Column(name = "MONTANT")
    private float montant;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DATEVIREMENT")
    @Temporal(TemporalType.DATE)
    private Date datevirement;
    @Column(name = "DATEEXECUTION")
    @Temporal(TemporalType.DATE)
    private Date dateexecution;
    @JoinColumn(name = "DEBITEUR", referencedColumnName = "NUMEROCOMPTE")
    @ManyToOne(optional = false)
    private Compte debiteur;
    @JoinColumn(name = "CREDITE", referencedColumnName = "NUMEROCOMPTE")
    @ManyToOne(optional = false)
    private Compte credite;

    public Virement() {
    }

    public Virement(Integer numerovirement) {
        this.numerovirement = numerovirement;
    }

    public Virement(Integer numerovirement, float montant, Date datevirement) {
        this.numerovirement = numerovirement;
        this.montant = montant;
        this.datevirement = datevirement;
    }

    public Integer getNumerovirement() {
        return numerovirement;
    }

    public void setNumerovirement(Integer numerovirement) {
        this.numerovirement = numerovirement;
    }

    public float getMontant() {
        return montant;
    }

    public void setMontant(float montant) {
        this.montant = montant;
    }

    public Date getDatevirement() {
        return datevirement;
    }

    public void setDatevirement(Date datevirement) {
        this.datevirement = datevirement;
    }

    public Date getDateexecution() {
        return dateexecution;
    }

    public void setDateexecution(Date dateexecution) {
        this.dateexecution = dateexecution;
    }

    public Compte getDebiteur() {
        return debiteur;
    }

    public void setDebiteur(Compte debiteur) {
        this.debiteur = debiteur;
    }

    public Compte getCredite() {
        return credite;
    }

    public void setCredite(Compte credite) {
        this.credite = credite;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (numerovirement != null ? numerovirement.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Virement)) {
            return false;
        }
        Virement other = (Virement) object;
        if ((this.numerovirement == null && other.numerovirement != null) || (this.numerovirement != null && !this.numerovirement.equals(other.numerovirement))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pridbank.model.entity.Virement[ numerovirement=" + numerovirement + " ]";
    }    
}
