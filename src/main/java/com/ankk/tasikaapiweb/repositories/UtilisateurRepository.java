package com.ankk.tasikaapiweb.repositories;
import com.ankk.taxsika.models.Utilisateur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UtilisateurRepository extends CrudRepository<Utilisateur, Long> {

    Utilisateur findByIdentifiant(String identifiant);
    Utilisateur findByEmail(String email);
    Utilisateur findByIdentifiantAndMotdepasse(String id, String pwd);
    List<Utilisateur> findAllByOrderByNomAsc();
    Utilisateur findByToken(String token);

    // Get History of TODAY :
    /*@Query(value = "SELECT * FROM Utilisateur j WHERE DATALENGTH(j.fcmtoken) > 0 and j.iduser <> ?1",
            nativeQuery = true)
    List<Utilisateur> findAllUsersWithNoFcmtoken(int iduser);*/
}
