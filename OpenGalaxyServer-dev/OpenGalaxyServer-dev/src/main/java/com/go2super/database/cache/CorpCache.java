package com.go2super.database.cache;

import com.go2super.database.entity.Corp;
import com.go2super.database.repository.CorpRepository;
import com.go2super.utility.GlueList;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class CorpCache {

    private static final CopyOnWriteArrayList<Corp> cache = new CopyOnWriteArrayList<>();

    private CorpRepository repository;

    @Autowired
    public void CorpCache(CorpRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());
    }

    public List<Corp> findAll() {

        return new GlueList<>(cache);
    }

    public void save(Corp value) {

        value = repository.save(value);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(Corp value) {

        remove(value);
    }

    public void remove(Corp value) {

        repository.delete(value);
        cache.remove(value);
    }

    public List<Corp> findByBlocId(ObjectId blocId) {

        return cache.stream().filter(value -> blocId.equals(value.getBlocId())).collect(Collectors.toList());
    }

    public List<Corp> findByCorpId(Collection<Integer> corpIds) {

        return cache.stream().filter(value -> corpIds.contains(value.getCorpId())).collect(Collectors.toList());
    }

    public Corp findByCorpId(int corpId) {

        return cache.stream().filter(value -> value.getCorpId() == corpId).findFirst().orElse(null);
    }

    public Corp findByAcronym(String acronym) {

        return cache.stream().filter(value -> acronym.equals(value.getAcronym())).findFirst().orElse(null);
    }

    public Corp findByGuid(int guid) {

        return cache.stream().filter(value -> value.getMembers() != null && value.getMembers().getMember(guid) != null).findFirst().orElse(null);
    }

    public List<Corp> findByCorpUpgrade() {

        return cache.stream().filter(value -> value.getCorpUpgrade() != null).collect(Collectors.toList());
    }

    public Corp findByName(String name) {

        return cache.stream().filter(value -> name.equals(value.getName())).findFirst().orElse(null);
    }

    public List<Corp> findRecruitsByGuid(int guid) {

        return cache.stream().filter(value -> value.getMembers() != null && value.getMembers().getRecruits() != null && value.getMembers().getRecruit(guid) != null).collect(Collectors.toList());
    }

    public List<Corp> findByStartWithName(String name) {

        return cache.stream().filter(value -> value.getName() != null && value.getName().startsWith(name)).collect(Collectors.toList());
    }

    public long count() {

        return cache.size();
    }

}
