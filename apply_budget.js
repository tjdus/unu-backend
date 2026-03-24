const fs = require('fs');
const BASE = 'C:/Users/2doky/Desktop/code/unu-frontend-main/unu-frontend-main';

function patch(filePath, oldStr, newStr) {
  let c = fs.readFileSync(filePath, 'utf8');
  if (c.includes(oldStr)) {
    c = c.replace(oldStr, newStr);
    fs.writeFileSync(filePath, c, 'utf8');
    console.log('patched:', filePath);
  } else {
    console.log('skip (already patched or not found):', filePath);
  }
}

// 1. menu-config.ts - 예산 관리 메뉴 추가
patch(
  BASE + '/lib/constants/menu-config.ts',
  `    {
      label: "활동 관리",
      href: "/manage/activities",
      icon: Calendar,
    },
  ],`,
  `    {
      label: "활동 관리",
      href: "/manage/activities",
      icon: Calendar,
    },
    {
      label: "예산 관리",
      href: "/manage/budget",
      icon: Wallet,
    },
  ],`
);

// Wallet import 추가
patch(
  BASE + '/lib/constants/menu-config.ts',
  `  Clock,`,
  `  Clock,
  Wallet,`
);

// 2. activity-creation-form.tsx - budget 필드 추가
// zod schema에 budget 추가
patch(
  BASE + '/components/custom/activity/activity-creation-form.tsx',
  `  parentActivityId: z.string().optional(),`,
  `  parentActivityId: z.string().optional(),
  budget: z.number().optional(),
  budgetNote: z.string().optional(),`
);

console.log('All patches applied!');
