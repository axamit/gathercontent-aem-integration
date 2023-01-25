package com.axamit.gc.core.sightly.containers;


import com.axamit.gc.core.sightly.models.FileAsset;
import com.day.cq.dam.api.Asset;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Model(adaptables = Resource.class)
public class FilesComponentContainer {
    private static final String DC_FORMAT = "dc:format";
    private static final String IMAGE_PATTERN = "image/";

    @Inject
    private String[] paths;

    @Inject
    private ResourceResolver resourceResolver;

    public List<FileAsset> getFiles() {
        List<FileAsset> files = new ArrayList<>();
        Arrays.stream(paths).forEach(path -> {
            Resource resource = resourceResolver.getResource(path);
            if (resource == null) {
                return;
            }
            Asset asset = resource.adaptTo(Asset.class);
            if (asset == null) {
                return;
            }
            Map<String, Object> metadata = asset.getMetadata();
            if (metadata.containsKey(DC_FORMAT)) {
                Object format = metadata.get(DC_FORMAT);
                if (StringUtils.startsWith((String) format, IMAGE_PATTERN)) {
                    files.add(new FileAsset(FileAsset.FileType.IMAGE, path, asset.getName()));
                    return;
                }
            }
            files.add(new FileAsset(FileAsset.FileType.OTHER, path, asset.getName()));
        });
        return files;
    }
}
