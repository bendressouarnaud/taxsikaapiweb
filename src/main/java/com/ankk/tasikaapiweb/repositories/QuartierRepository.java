package com.ankk.tasikaapiweb.repositories;

import com.ankk.taxsika.models.Mairie;
import com.ankk.taxsika.models.Profil;
import com.ankk.taxsika.models.Quartier;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface QuartierRepository extends CrudRepository<Quartier, Long> {
    List<Quartier> findAllByMairie(Mairie mairie);
    Optional<Quartier> findById(Long aLong);
}
