package com.lostark.backend.repository;

import com.lostark.backend.entity.Engraving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EngravingRepository extends JpaRepository<Engraving, Long> {
}
