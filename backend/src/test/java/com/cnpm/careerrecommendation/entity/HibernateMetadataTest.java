package com.cnpm.careerrecommendation.entity;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.PostgreSQLDialect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class HibernateMetadataTest {

    @Test
    @DisplayName("Validate Hibernate metadata build for all 10 entities using PostgreSQL dialect")
    void testHibernateMetadataBuild() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect", PostgreSQLDialect.class.getName())
            .build();
        try {
            MetadataSources sources = new MetadataSources(registry);
            sources.addAnnotatedClass(User.class);
            sources.addAnnotatedClass(CandidateProfile.class);
            sources.addAnnotatedClass(SkillCategory.class);
            sources.addAnnotatedClass(Skill.class);
            sources.addAnnotatedClass(UserSkill.class);
            sources.addAnnotatedClass(OccupationGroup.class);
            sources.addAnnotatedClass(Occupation.class);
            sources.addAnnotatedClass(OccupationSkill.class);
            sources.addAnnotatedClass(MarketDemandIndicator.class);
            sources.addAnnotatedClass(RecommendationLog.class);

            var metadata = sources.buildMetadata();
            assertNotNull(metadata);
            assertNotNull(metadata.getEntityBinding(User.class.getName()));
            assertNotNull(metadata.getEntityBinding(CandidateProfile.class.getName()));
            assertNotNull(metadata.getEntityBinding(SkillCategory.class.getName()));
            assertNotNull(metadata.getEntityBinding(Skill.class.getName()));
            assertNotNull(metadata.getEntityBinding(UserSkill.class.getName()));
            assertNotNull(metadata.getEntityBinding(OccupationGroup.class.getName()));
            assertNotNull(metadata.getEntityBinding(Occupation.class.getName()));
            assertNotNull(metadata.getEntityBinding(OccupationSkill.class.getName()));
            assertNotNull(metadata.getEntityBinding(MarketDemandIndicator.class.getName()));
            assertNotNull(metadata.getEntityBinding(RecommendationLog.class.getName()));
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}

