package com.snapandpack.repository;

import com.snapandpack.model.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for the editable site text. The built-in methods (findAll, save) are all it needs. */
public interface SiteSettingRepository extends JpaRepository<SiteSetting, String> {
}
