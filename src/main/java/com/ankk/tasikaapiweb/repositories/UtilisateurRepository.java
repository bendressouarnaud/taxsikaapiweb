package com.ankk.tasikaapiweb.repositories;
import com.ankk.taxsika.models.Mairie;
import com.ankk.taxsika.models.Profil;
import com.ankk.taxsika.models.Utilisateur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends CrudRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findAllByOrderByNomAsc();
    List<Utilisateur> findAllByProfil(Profil profil);
    List<Utilisateur> findAllByMairieAndProfilIn(Mairie mairie, List<Profil> profil);
    //Utilisateur findByIdentifiantAndMotdepasse(String id, String pwd);
    //
    //Utilisateur findByToken(String token);

    // Get History of TODAY :
    /*@Query(value = "SELECT * FROM Utilisateur j WHERE DATALENGTH(j.fcmtoken) > 0 and j.iduser <> ?1",
            nativeQuery = true)
    List<Utilisateur> findAllUsersWithNoFcmtoken(int iduser);*/
}
