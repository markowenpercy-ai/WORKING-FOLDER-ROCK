package com.go2super.database.cache;

import com.go2super.database.entity.User;
import com.go2super.database.repository.UserRepository;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class UserCache {

    private static final CopyOnWriteArrayList<User> cache = new CopyOnWriteArrayList<>();

    private UserRepository repository;

    @Autowired
    public void UserCache(UserRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());
    }

    public List<User> findAll() {

        return new GlueList<>(cache);
    }

    public void saveChanged() {

        try {
            var values = cache.stream().filter(User::isToSave).toList();
            if(values.isEmpty()){
                return;
            }
            values = repository.saveAll(values);
            for (User value : values) {
                value.setToSave(false);
                if (!cache.contains(value)) {
                    cache.add(value);
                }
            }
        } catch (Exception ex) {
            //save failed, ignore
        }
    }

    public void save(User value) {

        value = repository.save(value);
        value.setToSave(false);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(User user) {

        remove(user);
    }

    public void remove(User user) {

        repository.delete(user);
        cache.remove(user);
    }

    public List<User> findByShipBuilding() {

        return cache.stream().filter(user -> user.getShips().getFactory() != null && !user.getShips().getFactory().isEmpty()).collect(Collectors.toList());
    }

    public List<User> findByTechUpgrading() {

        return cache.stream().filter(user -> user.getTechs().getUpgrade() != null).collect(Collectors.toList());
    }

    public Optional<User> findById(String id) {

        return cache.stream().filter(user -> user.getId().toString().equals(id)).findFirst();
    }

    public Optional<User> findByUsername(String username) {

        return cache.stream().filter(user -> user.getUsername().equals(username)).findFirst();
    }

    public User findByUserId(long userId) {

        return cache.stream().filter(user -> user.getUserId() == userId).findFirst().orElse(null);
    }

    public List<User> findByAccountId(String accountId) {

        return cache.stream().filter(user -> user.getAccountId().equals(accountId)).collect(Collectors.toList());
    }

    public User findByGuid(int guid) {

        return cache.stream().filter(user -> user.getGuid() == guid).findFirst().orElse(null);
    }

    public List<User> findByGuid(Collection<Integer> guids) {

        return cache.stream().filter(user -> guids.contains(user.getGuid())).collect(Collectors.toList());
    }

}
