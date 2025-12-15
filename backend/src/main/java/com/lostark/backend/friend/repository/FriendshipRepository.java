package com.lostark.backend.friend.repository;

import com.lostark.backend.friend.entity.Friendship;
import com.lostark.backend.friend.entity.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByRequesterIdAndStatusOrderByCreatedAtDesc(Long requesterId, FriendshipStatus status);

    List<Friendship> findByAddresseeIdAndStatusOrderByCreatedAtDesc(Long addresseeId, FriendshipStatus status);

    @Query("""
            select f
            from Friendship f
            where
              (f.requester.id = :userA and f.addressee.id = :userB)
              or
              (f.requester.id = :userB and f.addressee.id = :userA)
            order by f.createdAt desc
            """)
    List<Friendship> findBetweenUsers(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query("""
            select f
            from Friendship f
            where
              (
                (f.requester.id = :userA and f.addressee.id = :userB)
                or
                (f.requester.id = :userB and f.addressee.id = :userA)
              )
              and f.status in :statuses
            order by f.createdAt desc
            """)
    Optional<Friendship> findLatestBetweenUsersWithStatuses(
            @Param("userA") Long userA,
            @Param("userB") Long userB,
            @Param("statuses") List<FriendshipStatus> statuses
    );

    @Query("""
            select (count(f) > 0)
            from Friendship f
            where
              (
                (f.requester.id = :userA and f.addressee.id = :userB)
                or
                (f.requester.id = :userB and f.addressee.id = :userA)
              )
              and f.status = com.lostark.backend.friend.entity.FriendshipStatus.ACCEPTED
            """)
    boolean existsAcceptedBetweenUsers(@Param("userA") Long userA, @Param("userB") Long userB);
}
