package autoparts.dao.file;

import autoparts.dao.CompatibilityDAO;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación basada en archivos planos para el acceso a datos de compatibilidad.
 * 
 * @author ALVARO
 */
public class CompatibilityFileDAO implements CompatibilityDAO {

    @Override
    public List<Integer> listVehicleIds(int componentId) {
        List<Integer> vehicleIds = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPATIBILITY)) {
            if (fields.length >= 2) {
                int vId = Integer.parseInt(fields[0].trim());
                int cId = Integer.parseInt(fields[1].trim());
                if (cId == componentId) {
                    vehicleIds.add(vId);
                }
            }
        }
        return vehicleIds;
    }

    @Override
    public List<Integer> listComponentIds(int vehicleId) {
        List<Integer> componentIds = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPATIBILITY)) {
            if (fields.length >= 2) {
                int vId = Integer.parseInt(fields[0].trim());
                int cId = Integer.parseInt(fields[1].trim());
                if (vId == vehicleId) {
                    componentIds.add(cId);
                }
            }
        }
        return componentIds;
    }

    @Override
    public void link(int vehicleId, int componentId) {
        String line = FileUtil.join(vehicleId, componentId);
        FileUtil.append(FlatFiles.COMPATIBILITY, line);
    }

    @Override
    public void unlink(int vehicleId, int componentId) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPATIBILITY)) {
            if (fields.length >= 2) {
                int vId = Integer.parseInt(fields[0].trim());
                int cId = Integer.parseInt(fields[1].trim());
                if (vId == vehicleId && cId == componentId) {
                    continue;
                }
            }
            lines.add(FileUtil.join((Object[]) fields));
        }
        FileUtil.rewrite(FlatFiles.COMPATIBILITY, lines);
    }
}
