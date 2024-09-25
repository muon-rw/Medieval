package dev.muon.medieval.leveling;

public class PlayerEnchantLevel {

    /*
    private static int calculateEnchantmentPower(List<Player> players) {
        int totalEnchantmentLevels = players.stream()
                .flatMap(player -> player.getInventory().armor.stream())
                .flatMap(itemStack -> EnchantmentHelper.getEnchantments(itemStack).entrySet().stream())
                .mapToInt(Map.Entry::getValue)
                .sum();

        int averageEnchantmentLevels = totalEnchantmentLevels / players.size();
        return (int) (averageEnchantmentLevels * MedievalConfig.get().levelsPerEnchantmentLevel);
    }
    */
}