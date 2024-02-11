package com.ankk.tasikaapiweb.repositories;
import com.ankk.taxsika.models.Profil;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProfilRepository extends CrudRepository<Profil, Long> {

    List<Profil> findAllByOrderByLibelleAsc();
    Optional<Profil> findById(Long id);
    Profil findByCode(String code);
    List<Profil> findByCodeNot(String code);

}
