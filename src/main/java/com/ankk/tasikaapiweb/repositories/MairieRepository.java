package com.ankk.tasikaapiweb.repositories;

import com.ankk.taxsika.models.Mairie;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MairieRepository extends CrudRepository<Mairie, Long> {

    List<Mairie> findAllByOrderByLibelleAsc();
    Mairie findByCode(String code);

}
