package org.alkemy.ong.ports.output.jpa;

import lombok.RequiredArgsConstructor;
import org.alkemy.ong.domain.model.Alkymer;
import org.alkemy.ong.domain.model.AlkymerList;
import org.alkemy.ong.domain.model.Skill;
import org.alkemy.ong.domain.repository.AlkymerRepository;
import org.alkemy.ong.ports.output.jpa.entity.AlkymerJpa;
import org.alkemy.ong.ports.output.jpa.mapper.AlkymerJpaMapper;
import org.alkemy.ong.ports.output.jpa.repository.AlkymerJpaRepository;
import org.alkemy.ong.ports.output.jpa.repository.SkillJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Primary
@Component
@RequiredArgsConstructor
public class AlkymerRepositoryImpl implements AlkymerRepository {

    private final AlkymerJpaRepository alkymerJpaRepository;
    private final SkillJpaRepository skillJpaRepository;
    private final AlkymerJpaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Alkymer> findOne(Long id) {
        return alkymerJpaRepository.findById(id).map(mapper::alkymerJpaToAlkymer);
    }

    @Override
    @Transactional
    public Long create(Alkymer alkymer) {
        AlkymerJpa alkymerJpa = mapper.alkymerToAlkymerJpa(alkymer);
        alkymer.skills().forEach(addSkillsToAlkymerJpa(alkymerJpa));

        return alkymerJpaRepository.save(alkymerJpa).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public AlkymerList findAllByPageable(Pageable pageable) {
        Page<AlkymerJpa> page = alkymerJpaRepository.findAll(pageable);
        List<Alkymer> list = mapper.alkymerJpaListToAlkymerList(page.getContent());

        return new AlkymerList(list, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        alkymerJpaRepository.findById(id).ifPresent(alkymerJpaRepository::delete);
    }

    @Override
    @Transactional
    public Optional<Alkymer> update(Long id, Alkymer alkymer) {
        return alkymerJpaRepository.findById(id)
                .map(alkymerJpa -> {
                    alkymerJpa.setStartDate(alkymer.startDate());
                    alkymerJpa.setEndDate(alkymer.startDate());
                    alkymer.skills().forEach(addSkillsToAlkymerJpa(alkymerJpa));
                    alkymerJpa = alkymerJpaRepository.save(alkymerJpa);

                    return mapper.alkymerJpaToAlkymer(alkymerJpa);
                });
    }

    private Consumer<Skill> addSkillsToAlkymerJpa(AlkymerJpa alkymerJpa) {
        return skill -> skillJpaRepository.findById(skill.id())
                .ifPresent(skillJpa -> alkymerJpa.getSkills().add(skillJpa));
    }
}
