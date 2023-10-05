package net.stelitop.battledudestcg.commons.utils;

//@Service
//public class ExcelUtils implements ApplicationRunner {
//
//    @Autowired
//    private CardRepository cardRepository;
//
//    private ElementalType parseElemType(String str) {
//        if (str.isBlank()) throw new IllegalArgumentException();
//        String s = str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
//        return ElementalType.valueOf(s);
//    }
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        System.out.println("Opening excel sheet:");
//        //FileInputStream file = new FileInputStream(new File("BattleDudes.xlsx"));
//        Workbook workbook = new XSSFWorkbook(file);
//        Sheet sheet = workbook.getSheet("Dudes");
//        if (sheet == null) {
//            System.out.println("No sheet named \"Dudes\" found!");
//            return;
//        }
//        int i = -1;
//        for (Row row : sheet) {
//            i++;
//            if (i == 0) continue;
//            System.out.println("Row = " + i + ": ");
//            // 17
//            if (row.getLastCellNum() != 17) continue;
//            String dudeIdStr = row.getCell(15).getStringCellValue();
//            if (!dudeIdStr.startsWith("#")) continue;
//            long dudeId = Long.parseLong(dudeIdStr.substring(1));
//            var dudeOpt = cardRepository.findDudeByDudeId(dudeId);
//            if (dudeOpt.isEmpty()) {
//                System.out.println("Could not find dude with dude id = " + dudeId);
//                continue;
//            }
//            DudeCard dude = dudeOpt.get();
//            dude.setCost((int)row.getCell(7).getNumericCellValue());
//            dude.setAttack((int)row.getCell(8).getNumericCellValue());
//            dude.setHealth((int)row.getCell(9).getNumericCellValue());
//            dude.setEffectText(row.getCell(10).getStringCellValue());
//            dude.setRarity(Rarity.valueOf(row.getCell(11).getStringCellValue()));
//            List<ElementalType> types = new ArrayList<>();
//            if (!row.getCell(4).getStringCellValue().equals("N/A")) {
//                types.add(parseElemType(row.getCell(4).getStringCellValue()));
//                if (!row.getCell(5).getStringCellValue().equals("N/A")) {
//                    types.add(parseElemType(row.getCell(5).getStringCellValue()));
//                    if (!row.getCell(6).getStringCellValue().equals("N/A")) {
//                        types.add(parseElemType(row.getCell(6).getStringCellValue()));
//                    }
//                }
//            }
//            dude.setTypes(types);
//            cardRepository.save(dude);
//        }
//    }
//}
