package com.ankk.tasikaapiweb.repositories;

import com.ankk.taxsika.models.Commercant;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommercantRepository extends CrudRepository<Commercant, Long> {
}