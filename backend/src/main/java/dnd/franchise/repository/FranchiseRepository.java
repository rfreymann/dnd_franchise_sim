package com.dnd.franchise.repository;

import com.dnd.franchise.model.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FranchiseRepository extends JpaRepository<Franchise, Long> {}
