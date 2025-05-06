package pots.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pots.entity.KYCEntity

@Repository
interface KYCRepository : JpaRepository<KYCEntity, Long> {}