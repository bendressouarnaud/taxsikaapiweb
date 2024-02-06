package com.ankk.tasikaapiweb.repositories;
import com.ankk.taxsika.models.Profil;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProfilRepository extends CrudRepository<Profil, Long> {

    List<Profil> findAllByOrderByLibelleAsc();

}
