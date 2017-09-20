package pridbank.facade;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import pridbank.controller.DateFactice;
import pridbank.model.entity.Compte;
import pridbank.model.entity.Utilisateur;
import pridbank.model.entity.Virement;
import pridbank.ressources.PasswordStorage;

@Stateless
public class ModelFacade {
    
    @Inject
    Event<FacesMessage> afficheurMessage;
    
    @Inject
    DateFactice dateFactice;

    public void afficherMessage(FacesMessage message) {
        afficheurMessage.fire(message);
    }
    
    @PersistenceContext(unitName = "pridbank_groupe12PU")
    private EntityManager em;

    public void clearCache() {
        em.getEntityManagerFactory().getCache().evictAll();
    }

    protected void removeEntityFromCache(Class cls, Object primaryKey) {
        em.getEntityManagerFactory().getCache().evict(cls, primaryKey);
    }
    
    public Utilisateur trouverUtilisateur(int idClient) {
        Query requete = em.createNamedQuery("Utilisateur.trouverUtilisateurParId");
        requete.setParameter("idClient", idClient);
        Utilisateur utilisateur = requete.getResultList().isEmpty() ? null : (Utilisateur) requete.getResultList().get(0);
        return utilisateur;
    }

    public boolean verifierNomUtilisateur(String nomUtilisateur) {
        if (nomUtilisateur.length() < 3){
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "La longeur du nom doit être suppérieure à 3."));
            return false;
        } 
        Utilisateur utilisateur = trouverUtilisateur(nomUtilisateur);
        if (utilisateur != null) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", nomUtilisateur+" existe déjà..."));
            return false;
        }
        return true;
    }
    
    public Utilisateur trouverUtilisateur(String nomUtilisateur) {
        if (nomUtilisateur == null || nomUtilisateur.isEmpty()) {
            return null;
        } else {
            Query requete = em.createNamedQuery("Utilisateur.trouverUtilisateurParNom");
            requete.setParameter("nomUtilisateur", nomUtilisateur);
            Utilisateur utilisateur = requete.getResultList().isEmpty() ? null : (Utilisateur) requete.getResultList().get(0);
            // force a refresh of the cache from the DB (this solution is more portable)
            if (utilisateur != null) {
                em.refresh(utilisateur);
            }
            return utilisateur;
        }
    }
    
    public boolean ageMinimalRequis(Date date) {
        if (date != null) {
            LocalDate naissance = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate aujourdhui = new java.sql.Date(dateFactice.getDateFactice().getTime()).toLocalDate() ;// .toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); //LocalDate.now();
            if (Period.between(naissance, aujourdhui).getYears() >= 18) {
                return true;
            }                
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Age minimum: 18 ans"));
            return false;             
        }  
        return false;
    }

    public boolean longeurMotDePasseOK(String mdp){
        if (mdp.length() >= 3 && mdp.length() <= 20)
            return true;
        afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Mot de passe doit faire entre 3 et 20 caractères."));
        return false;
    }
    
    public boolean confirmMotDePasseOK(String confirm, String mdp){
        if (mdp.equals(confirm))
            return true;
        afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Le mot de passe et la confirmation doivent être identiques."));
        return false;
    }
    
    public Utilisateur creerUtilisateur(String nomUtilisateur, String motDePasse, String confirmationMotDePasse, Date dateDeNaissance, boolean estUnGestionnaire, List<Compte> listeComptes) {
        Utilisateur utilisateur = trouverUtilisateur(nomUtilisateur);
        boolean erreur = false;
        if(utilisateur != null) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Cet utilisateur existe déjà."));
            erreur = true;
        } if(!motDePasse.equals(confirmationMotDePasse)) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Les deux mots de passe doivent être identiques."));
            erreur = true;
        } if(!ageMinimalRequis(dateDeNaissance)) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Tout utilisateur doit être âgé de 18 ans ou plus."));
            erreur = true;
        } if(!erreur) { 
            return nouvelUtilisateur(nomUtilisateur, motDePasse, dateDeNaissance, estUnGestionnaire, listeComptes);
        }
        return null;
    }

    private Utilisateur nouvelUtilisateur(String nomUtilisateur, String motDePasse, Date dateDeNaissance, boolean estUnGestionnaire, List<Compte> comptesLies) {
        String hashMotDePasse = new String();
        try {
            hashMotDePasse = PasswordStorage.createHash(motDePasse);
        } catch (Exception e) {}
        Utilisateur utilisateur = new Utilisateur(null, nomUtilisateur, hashMotDePasse, dateDeNaissance, estUnGestionnaire);
        em.persist(utilisateur); // pourquoi en deux étapes ?
        em.flush();
        if(!utilisateur.getEstUnGestionnaire() && !comptesLies.isEmpty()) {
            utilisateur.setListeDesComptes(comptesLies);
            em.merge(utilisateur);
        }       
        return utilisateur;
    }
    
    public Utilisateur modifierUtilisateur(Utilisateur utilisateurSelectionne, String motDePasse, String confirmationMotDePasse) {
        String hashMotDePasse = new String();
        try {
            hashMotDePasse = PasswordStorage.createHash(motDePasse);
        } catch (Exception e) {}
        
        Utilisateur utilisateurOriginal = em.find(Utilisateur.class, utilisateurSelectionne.getIdClient());
        boolean modificationEffective = false;
        if(utilisateurSelectionne.getNomUtilisateur() != null && !utilisateurSelectionne.getNomUtilisateur().isEmpty() && !utilisateurSelectionne.getNomUtilisateur().equals(utilisateurOriginal.getNomUtilisateur())) {
            Utilisateur utilisateur = trouverUtilisateur(utilisateurSelectionne.getNomUtilisateur());
            if(utilisateur != null) {
                afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Ce nom d'utilisateur est déjà utilisé."));
                return null;
            } else {
                utilisateurOriginal.setNomUtilisateur(utilisateurSelectionne.getNomUtilisateur());
                modificationEffective = true;
            }
        } if(utilisateurSelectionne.getDateDeNaissance() != null && !utilisateurOriginal.getDateDeNaissance().equals(utilisateurSelectionne.getDateDeNaissance())) {
            if(!ageMinimalRequis(utilisateurSelectionne.getDateDeNaissance())) {
                afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Tout utilisateur doit être âgé de 18 ans minimum."));
                return null;
            } else {
                utilisateurOriginal.setDateDeNaissance(utilisateurSelectionne.getDateDeNaissance());
                modificationEffective = true;
            }
        } if(motDePasse != null && !motDePasse.isEmpty()) {
            if(confirmationMotDePasse == null) {
                afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Vous devez confirmer le mot de passe."));
                return null;
            } else if(!confirmationMotDePasse.equals(motDePasse)) {
                afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Les deux mots de passe doivent être identiques."));
                return null;
            } else {
                utilisateurOriginal.setMotDePasse(hashMotDePasse);
                modificationEffective = true;
            }
        } if(modificationEffective) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Sauvegarde réussie."));
            return rafraichirUtilisateur(utilisateurOriginal);
        }
        return null;
    }
    
    public Utilisateur rafraichirUtilisateur(Utilisateur utilisateur) {
        em.merge(utilisateur);
        clearCache();
        return utilisateur;
    }

    public void sauvegarderUtilisateur(Utilisateur utilisateur) { // devrait être remplacée par celle qui return l'Utilisateur
        em.merge(utilisateur);
    }
    
    private boolean suppressionGestionnaire(Utilisateur gestionnaire, int idUtilisateurCourant) {
        int nombreGestionnaires = ((Number)em.createNamedQuery("Utilisateur.compterGestionnaires").getSingleResult()).intValue();
        if(gestionnaire.getIdClient()==idUtilisateurCourant) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Un gestionnaire ne peut pas se supprimer lui-même."));
            return false;
        } else if(nombreGestionnaires > 1) {
            em.remove(gestionnaire);
            clearCache();
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Le gestionnaire '" + gestionnaire.getNomUtilisateur() + "' (ID " + gestionnaire.getIdClient() + ") a bien été supprimé."));
            return true;
        } else {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible de supprimer le dernier gestionnaire."));
            return false;
        }
    }
    
    private boolean suppressionClient(Utilisateur client) {
        List<Compte> listeComptes = client.getListeDesComptes();
        if(!listeComptes.isEmpty()) {
            for(int i = 0; i < listeComptes.size() ; i++) { //ici c'est normal qu'on parcoure la liste de cette manière triviale
                                                            //mais une boucle "foreach" ne permet pas de supprimer des éléments de la dite boucle
                if(listeComptes.get(i).getListeProprietaires().size() == 1) {
                    if(listeComptes.get(i).getListeProprietaires().get(0).equals(client)) {
                        listeComptes.get(i).getListeProprietaires().remove(client);
                        Compte aSupprimer = listeComptes.get(i);
                        client.getListeDesComptes().remove(listeComptes.get(i));
                        em.merge(aSupprimer);
                        em.remove(aSupprimer);
                        i--;
                        afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Le compte n° "+aSupprimer.getNumero()+" a été supprimé, étant possédé uniquement par "+client.getNomUtilisateur()+". Le solde a été versée à la banque!!"));
                    } else {
                        afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur de synchronisation."));
                        return false;
                    }
                }
            }
        }
        em.merge(client);
        em.remove(client);
        afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Le client '" + client.getNomUtilisateur() + "' (ID " + client.getIdClient() + ") a bien été supprimé."));
        clearCache();
        return true;
    }

    public boolean supprimerUtilisateur(int idUtilisateur, int idUtilisateurCourant) {
        Utilisateur utilisateur = em.find(Utilisateur.class, idUtilisateur);
        if (utilisateur == null) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "L'utilisateur sélectionné n'existe pas."));
            return false;
        } else {
            if(utilisateur.getEstUnGestionnaire()) {
                return suppressionGestionnaire(utilisateur,idUtilisateurCourant);
            } else {
                return suppressionClient(utilisateur);
            }
        }
    }
    
    public List<Utilisateur> getListeUtilisateurs() {
        Query requete = em.createNamedQuery("Utilisateur.findAll");
        return requete.getResultList();
    }
    
    public List<Utilisateur> getClients() {
        Query requete = em.createNamedQuery("Utilisateur.findClients");
        return requete.getResultList();
    }
    
    public Utilisateur lierComptesClient(Utilisateur utilisateur, List<Compte> listeComptes) {
        utilisateur = em.find(Utilisateur.class, utilisateur.getIdClient());
        String erreur = "" ;
        String outro = " car un compte ne peut pas exister sans propriétaire.";
        if (!utilisateur.getListeDesComptes().isEmpty()){
            for (Compte compteActuelDuClient : utilisateur.getListeDesComptes()){
                if (!listeComptes.contains(compteActuelDuClient) && compteActuelDuClient.getListeProprietaires().size() == 1 ){
                    if (erreur.equals(""))
                        erreur = "Un compte n'a pas pu être dissocié de " + utilisateur.getNomUtilisateur();
                    else
                        erreur = "Certains comptes n'ont pas pu être dissociés de " + utilisateur.getNomUtilisateur();
                    listeComptes.add(compteActuelDuClient);
                }
            }
        }
        utilisateur.setListeDesComptes(listeComptes);
        em.merge(utilisateur);
        clearCache();
        if (!erreur.equals("")){
            erreur = erreur + outro ;
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", erreur));
        }
        else{
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Sauvegarde réussie."));            
        }
        return utilisateur;
    }
    
    public List<Utilisateur> getClientsCompte(Compte compte) {
        return compte.getListeProprietaires();
    }
    
    public List<Utilisateur> getNonClientsCompte(Compte compte) {
        Query requete = em.createNamedQuery("Utilisateur.findClientsNonLiesAuCompte");
        requete.setParameter("compte", compte);
        return requete.getResultList();
        
    }

    public List<Compte> getComptesDuClient(Utilisateur utilisateur) {
        return utilisateur.getListeDesComptes();
    }
    
    public List<Compte> getNonComptesDuClient(Utilisateur utilisateur) {
        Query requete = em.createNamedQuery("Compte.findComptesNonLiesAuClient");
        requete.setParameter("utilisateur", utilisateur);
        return requete.getResultList();
    }
    
    public void verifierSoldeCreation(Float solde) {
        if(solde != null && solde < 0) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", " doit être > 0"));
        }
    }
    
    public Compte trouverCompte(int numeroCompte) {
        Query requete = em.createNamedQuery("Compte.trouverCompteParNumero");
        requete.setParameter("numeroCompte", numeroCompte);
        Compte compte = requete.getResultList().isEmpty() ? null : (Compte) requete.getResultList().get(0);
        // force a refresh of the cache from the DB (this solution is more portable)
        if (compte != null) {
            em.refresh(compte);
        }
        return compte;
    }
    
    private boolean typeValide(char type) {
        return Compte.getPlancher().containsKey(type);
    }
    
    public Compte creerCompte(char type, Float solde, boolean estBloque, List<Utilisateur> listeProprietaires) {
        if(solde == null) solde = new Float(0);
        boolean erreur = false;
        if(!typeValide(type)) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "\"" + type + "\" n'est pas un type de compte valide."));
            erreur = true;
        } if(solde < 0) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le solde ne peut être négatif lors de la création du compte."));
            erreur = true;
        } if(listeProprietaires.isEmpty()) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Un compte doit avoir minimum un propriétaire."));
            erreur = true;
        } if(!erreur) {
            return nouveauCompte(type, solde, estBloque, listeProprietaires);
        }
        return null;
    }
    
    public Compte nouveauCompte(char type, float solde, boolean estBloque, List<Utilisateur> listeProprietaires) {
        Compte compte = new Compte(null, type, solde, estBloque);
        compte.setListeProprietaires(listeProprietaires);
        em.persist(compte);
        em.flush();
        clearCache();
        return compte;
    }
    
    public Compte modifierCompte(Compte compte, char type, boolean estBloque) {
        if(!typeValide(type)) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le type sélectionné n'est pas un type valide."));
        } else if(compte.getSolde() < Compte.getPlancher().get(type)) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible de modifier le type de ce compte car son solde est insuffisant."));
        } else {
            compte = em.find(Compte.class, compte.getNumero());
            compte.setType(type);
            compte.setEstBloque(estBloque);
            em.merge(compte);
            clearCache();
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Sauvegarde réussie."));
            return compte;
        }
        return null;
    }
    
    public Compte lierClientsCompte(Compte compte, List<Utilisateur> listeProprietaires) {
        if(listeProprietaires.isEmpty()) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Un compte doit avoir au moins un propriétaire."));
        } else {
            compte = em.find(Compte.class, compte.getNumero());
            compte.setListeProprietaires(listeProprietaires);
            em.merge(compte);
            clearCache();
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Sauvegarde réussie."));
            return compte;
        }
        return null;
    }
    
    public void bloquerCompte(Compte compte) {
        compte = em.find(Compte.class, compte.getNumero());
        compte.setEstBloque(!compte.getEstBloque());
        em.merge(compte);
        clearCache();
        afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Le compte n°" + compte.getNumero() + " a été " + (compte.getEstBloque() ? "bloqué" : "débloqué") + "."));
    }
    
    public void supprimerCompte(int numeroCompte) {
        Compte compte = em.find(Compte.class, numeroCompte);
        for (Utilisateur client : compte.getListeProprietaires()){
            client.getListeDesComptes().remove(compte);
            em.merge(client);
        }
        compte.setListeProprietaires(new ArrayList<>());
        em.merge(compte);
        em.remove(compte);
        afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Le compte n° " + numeroCompte + " a été supprimé avec succès."));
    }
    
    public List<Compte> getListeComptes() {
        Query requete = em.createNamedQuery("Compte.findAll");
        return requete.getResultList();
    }
    
    private void paiement(Virement virement) {
        // Ici, on considère que toutes les vérifications métier sont faites (dont la vérification de la date)
        virement.setDateexecution(dateFactice.getDateFactice());
        Compte debiteur = virement.getDebiteur();
        Compte credite = virement.getCredite();
        virement = em.merge(virement);
        debiteur.setSolde(debiteur.getSolde() - virement.getMontant());
        debiteur.getVirementsSortants().add(virement);
        em.merge(debiteur);
        credite.getVirementsEntrants().add(virement);
        credite.setSolde(credite.getSolde() + virement.getMontant());
        em.merge(credite);
        em.flush();
    }
    
    private void paiementVirementSauvegarde(Virement virement) {
        // Ici, on considère que toutes les vérifications métier sont faites (dont la vérification de la date)
        virement.setDateexecution(dateFactice.getDateFactice());
        Compte debiteur = virement.getDebiteur();
        Compte credite = virement.getCredite();
        virement = em.merge(virement);
        debiteur.setSolde(debiteur.getSolde() - virement.getMontant());
        em.merge(debiteur);
        credite.setSolde(credite.getSolde() + virement.getMontant());
        em.merge(credite);
        em.flush();
    }
    
    private List<String> verificationsBasiquesVirement(Virement virement) {
        List<String> resultat = new ArrayList<>();
        if(virement.getDebiteur().getNumero().equals(virement.getCredite().getNumero())) {
            resultat.add("Un compte ne peut se faire un virement à lui-même.");
        } if(virement.getMontant() <= 0) {
            resultat.add("Le montant du virement doit être supérieur à 0.");
        } if(autoriserVirementSortantSurBlocageCompteCredite(virement)) {
            resultat.add("Impossible d'effectuer un virement sur un compte bloqué (n° " + virement.getCredite().getNumero() + ").");
        } if(autoriserVirementSortantSurBlocageCompteDebiteur(virement)) {
            resultat.add("Impossible d'effectuer un virement depuis un compte bloqué (n° " + virement.getDebiteur().getNumero() + ").");
        }
        return resultat;
    }
    
    private List<String> verificationsTechniquesVirement(Virement virement) {
        List<String> resultat = new ArrayList<>();
        float sommeVirementExecuteSemaine = sommeVirementExecuteSemaine(virement.getDebiteur());
        if(!autoriserVirementSortantSurSomme(virement)) {
            resultat.add("Le montant du virement dépasse les capacités du compte."); // ATTENTION en cas de modif, modifier sous executerTousVirements()
        } if(sommeVirementExecuteSemaine > 10000) {
            resultat.add("Le plafond hebdomadaire de 10.000€ a déjà été atteint par ce compte.");
        } else if(sommeVirementExecuteSemaine + virement.getMontant() > 10000) {
            resultat.add("Impossible d'effectuer ce virement car le compte sélectionné dépasserait la limite hebdomadaire de 10.000€.");
        }
        return resultat;
    }
    
    public List<String> verificationCompleteVirement(Virement virement) {
        List<String> resultat = verificationsBasiquesVirement(virement);
        resultat.addAll(verificationsTechniquesVirement(virement));
        return resultat;
    }
    
    private boolean sauvegarderVirement(Virement virement) {
        boolean retour = true;
        if(virement.getDebiteur().getNumero().equals(virement.getCredite().getNumero())) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Un compte ne peut se faire un virement à lui-même."));
            retour = false;
        } 
        if(virement.getDatevirement().before(dateFactice.getDateFactice())) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "La date sélectionnée doit être une date future."));
            retour = false;
        } 
        if (virement.getMontant()<= 0){
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le montant du virement doit être supérieur à 0"));
            retour = false;
        } 
        if(retour) {
            Compte debiteur = virement.getDebiteur();
            virement = em.merge(virement);
            debiteur.getVirementsSortants().add(virement);
            em.merge(debiteur);
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Le virement a été enregistré avec succès."));
        } 
        return retour;
    }
    
    public boolean autoriserVirementSortantSurSomme(Virement virement) {
        // "moins moins" = "plus"
        float sommeDisponible = virement.getDebiteur().getSolde() - virement.getDebiteur().getPlancher().get(virement.getDebiteur().getType());
        return sommeDisponible >= virement.getMontant();
    }
    
    public boolean autoriserVirementSortantSurBlocageCompteCredite(Virement virement) {
        return virement.getCredite().getEstBloque();
    }
    
    public boolean autoriserVirementSortantSurBlocageCompteDebiteur(Virement virement) {
        return virement.getDebiteur().getEstBloque();
    }
    
    public boolean formulaireVirementInvalide(Compte compteSelectionne, Compte compteDestinataire, float sommeVirement, Date dateExecution) {
        return compteSelectionne == null || compteDestinataire == null || sommeVirement <= 0 || getZeroTimeDate(dateExecution).before(getZeroTimeDate((Date) dateFactice.getDateFactice()));
    }
    
    public float sommeDisponiblePourVirement(Compte compte) { // méthode déplacée ici car c'est une règle métier, ràf dans le controller
        float sommeRestant = 10000 - sommeVirementExecuteSemaine(compte);
        float soldeDisponible = (0 - compte.getPlancher().get(compte.getType())) + compte.getSolde();
        if(sommeRestant >= soldeDisponible) {
            return soldeDisponible;
        } else if(sommeRestant <= 0) {
            return 0;
        }
        return sommeRestant;
    }
    
    private float sommeVirementExecuteSemaine(Compte compte) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.dateFactice.getDateFactice());
        cal.add(Calendar.DATE, -7);
        Date date = cal.getTime();
        float sommeVirementSemaine = 0;
        if(!compte.getVirementsSortants().isEmpty()) {
            for(Virement v : compte.getVirementsSortants()) {
                if(v.getDateexecution() != null && !v.getDateexecution().before(date) && !v.getDateexecution().after(this.dateFactice.getDateFactice())) {
                    sommeVirementSemaine += v.getMontant();
                }
            }
        }       
        return sommeVirementSemaine;
    }
    
    public boolean preparerExecutionVirement(Compte compteSelectionne, Compte compteDestinataire, float sommeVirement, Date dateExecution) {
        if(compteSelectionne == null) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Vous devez sélectionner un compte débiteur."));
            return false;
        } if(compteDestinataire == null) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Vous devez sélectionner un compte à créditer ou entrer un numéro de compte valide."));
            return false;
        } if(dateExecution == null) {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Vous devez sélectionner une date pour l'exécution du virement."));
            return false;
        }
        Virement virement = new Virement();
        virement.setDebiteur(compteSelectionne);
        virement.setCredite(compteDestinataire);
        virement.setDatevirement(dateExecution);
        virement.setDateexecution(null);
        virement.setMontant(sommeVirement);
        if(getZeroTimeDate(virement.getDatevirement()).equals(getZeroTimeDate((Date) dateFactice.getDateFactice()))) {
            if(executerVirement(virement)) {
                return true;
            }
        } else if(sauvegarderVirement(virement)) {
            return true;
        } 
        return false;
    }
    
    private boolean executerVirement(Virement virement) {
        List<String> retour = verificationCompleteVirement(virement);
        if(!retour.isEmpty()) {
            if(virement.getNumerovirement() != null){
                supprimerVirement(virement); //Cela permet d'effacer un virement de la DB, la vérification de l'existence d'un n° c'est pour les virements qui ne sont pas encore en DB
            }
            for(String erreur : retour) {
                afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", erreur));
            }
            return false;
        } else {
            paiement(virement);
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Le virement a été exécuté avec succès."));
            return true;
        }
    }
    
    public HashMap<Virement, List<String>> executerTousVirements() {
        Query requete = em.createNamedQuery("Virement.trouverVirementsAExecuter");
        requete.setParameter("datevirement", dateFactice.getDateFactice());
        List<Virement> virementsAExecuter = requete.getResultList();
        HashMap<Virement, List<String>> resultat = new HashMap<>();
        if(!virementsAExecuter.isEmpty()) {
            List<Virement> virementsEnAttente = new ArrayList<>();
            for(Virement virement : virementsAExecuter) {
                List<String> retour = verificationCompleteVirement(virement);
                if(retour.isEmpty()) {
                    paiementVirementSauvegarde(virement);
                } else if(retour.size() == 1 && retour.get(0).equals("Le montant du virement dépasse les capacités du compte.")) {
                    virementsEnAttente.add(virement);
                } else {
                    resultat.put(virement, retour);
                    supprimerVirement(virement);
                }
            }
            if(!virementsEnAttente.isEmpty()) {
                boolean reparcourir = false;
                do {
                    List<Virement> virementsASupprimer = new ArrayList<>();
                    for(Virement virement : virementsEnAttente) {
                        List<String> retour = verificationsTechniquesVirement(virement);
                        if(retour.isEmpty()) {
                            paiementVirementSauvegarde(virement);
                            virementsASupprimer.add(virement);
                        } else if(retour.size() == 1 && retour.get(0).equals("Le montant du virement dépasse les capacités du compte.")) {
                            reparcourir = true;
                        } else {
                            resultat.put(virement, retour);
                            supprimerVirement(virement);
                            virementsASupprimer.add(virement);
                        }
                    }
                    if(!virementsASupprimer.isEmpty()) {
                        for(Virement virement : virementsASupprimer) {
                            virementsEnAttente.remove(virement);
                        }
                    } else if(reparcourir) {
                        reparcourir = false;
                    }
                } while(reparcourir);
            }
            if(!virementsEnAttente.isEmpty()) {
                for(Virement virement : virementsEnAttente) {
                    List<String> retour = verificationsTechniquesVirement(virement);
                    resultat.put(virement, retour);
                    supprimerVirement(virement);
                }
            }
            if(resultat.isEmpty()) {
                if(virementsAExecuter.size() == 1)
                    afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Il n'y avait qu'une opération et elle a été exécutée avec succès."));
                else
                    afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Toutes les opérations ont été effectuées avec succès."));
            }
        }
        return resultat;
    }
    
    public List<Virement> trouverTousVirementsAExecuter(){
        Query requete = em.createNamedQuery("Virement.trouverVirementsAExecuter");
        requete.setParameter("datevirement", dateFactice.getDateFactice());
        List<Virement> virementsAExecuter = requete.getResultList();
        return virementsAExecuter;
    }
    
    public Virement trouverVirement(int numeroVirement) {
        Query requete = em.createNamedQuery("Virement.findByNumerovirement");
        requete.setParameter("numerovirement", numeroVirement);
        Virement virement = requete.getResultList().isEmpty() ? null : (Virement) requete.getResultList().get(0);
        // force a refresh of the cache from the DB (this solution is more portable)
        if (virement != null) {
            em.refresh(virement);
        }
        return virement;
    }
    
    public void supprimerVirement(Virement virement){
        Virement virementAEffacer = trouverVirement(virement.getNumerovirement());
        //em.merge(virementAEffacer);
        if (virementAEffacer.getDateexecution() == null){
            virement.getCredite().getVirementsEntrants().remove(virement);
            virement.getDebiteur().getVirementsSortants().remove(virement);
            em.remove(virementAEffacer);
            em.merge(virement.getCredite());
            em.merge(virement.getDebiteur());
        } else {
            afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le virement est déjà executé, on ne peut plus le supprimer."));
        }
    }
    
    private Date getZeroTimeDate(Date fecha) {
        Date res = fecha;
        Calendar calendar = Calendar.getInstance();

        calendar.setTime( fecha );
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        res = calendar.getTime();

        return res;
    }
}
