package com.ankk.tasikaapiweb.securite;

import com.ankk.tasikaapiweb.repositories.ProfilRepository;
import com.ankk.tasikaapiweb.repositories.UtilisateurRepository;
import com.ankk.taxsika.models.Profil;
import com.ankk.taxsika.models.Utilisateur;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    ProfilRepository profilRepository;
    @PersistenceUnit
    EntityManagerFactory emf;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        //return UserDetailsImpl.build(utilisateur);
        //
        User.UserBuilder builder = null;
        if (utilisateur != null) {
            builder =
                    User.withUsername(username);
            builder.password(utilisateur.getMotdepasse());
            //
            Profil profil =null;//= profilRepository.findByIdpro(utilisateur.getProfil());
            builder.roles("admin");
        } else {
            throw new UsernameNotFoundException("User not found.");
        }
        return builder.build();
    }
}
