package com.ankk.tasikaapiweb.repositories;

import com.ankk.tasikaapiweb.projections.BeanQuartierSecteurProjection;
import com.ankk.taxsika.models.Quartier;
import com.ankk.taxsika.models.Secteur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SecteurRepository extends CrudRepository<Secteur, Long> {
    List<Secteur> findAllByQuartierInOrderByLibelleAsc(List<Quartier> liste);

    @Query(value = "select distinct b.id as idqua,b.libelle as libquart," +
            "c.id as idsec, c.libelle as libsec from " +
            "mairie a inner join quartier b on a.id=b.mairie_id " +
            "inner join secteur c on c.quartier_id=b.id where a.id = ?1",
            nativeQuery = true)
    List<BeanQuartierSecteurProjection> findAllSecteurByMairie(long idmai);
}
