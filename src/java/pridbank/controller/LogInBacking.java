package pridbank.controller;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import pridbank.model.entity.Utilisateur;
import pridbank.ressources.PasswordStorage;

@Named
@SessionScoped
public class LogInBacking extends Base {

    private static final long serialVersionUID = 1L;
    
    private String nomUtilisateur;
    private String motDePasse;
    private Utilisateur utilisateurCourant;

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
        this.motDePasse = trimValue(motDePasse);
    }
    
    public Utilisateur getUtilisateurCourant() {
        return this.utilisateurCourant;
    }
    
    public String login() {
        Utilisateur utilisateur = model.trouverUtilisateur(nomUtilisateur);
        try{
            if (utilisateur == null) {
               model.afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Cet utilisateur n'existe pas."));
            } else if (!PasswordStorage.verifyPassword(motDePasse, utilisateur.getMotDePasse())) {
               model.afficherMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le mot de passe est incorrect."));
            } else {
                utilisateurCourant = utilisateur;
                nomUtilisateur = null;
                motDePasse = null;
                if(utilisateurCourant.getEstUnGestionnaire()){
                    return "/protected/gestionnaire/index.xhtml?faces-redirect=true";
                }
                return "/protected/client/index.xhtml?faces-redirect=true";
            }
        }catch (Exception e){}
        
        return null;
    }
    
    public String logout() {
        unlog();
        return "/index.xhtml?faces-redirect=true";
    }
    
    public void unlog() {
        utilisateurCourant = null;
    }
    
    public void redirigerProtected() {
        if(utilisateurCourant != null) {
            if(utilisateurCourant.getEstUnGestionnaire()) {
                rediriger("gestionnaire/index.xhtml");
            } else {
                rediriger("client/index.xhtml");
            }
        }
        rediriger("/index.xhtml");
    }
    
    public void redirigerLogin() {
        if(utilisateurCourant != null) {
            if(utilisateurCourant.getEstUnGestionnaire()) {
                rediriger("protected/gestionnaire/index.xhtml");
            } else {
                rediriger("protected/client/index.xhtml");
            }
        }
    }
    
    private void rediriger(String redirection) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
        try {
            response.sendRedirect(redirection);
        } catch(Exception e) {
            try {
                response.sendRedirect("/index.xhtml");
            } catch(Exception ex) {}
        }
    }
    
    private String trimValue(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        } else {
            return value.trim();
        }
    }
}
