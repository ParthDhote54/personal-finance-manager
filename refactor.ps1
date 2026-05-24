$ErrorActionPreference = "Stop"

$src = "p:\Personal Finance Manager\personal-finance-manager\src\main\java\com\personal\finance\manager"
$testSrc = "p:\Personal Finance Manager\personal-finance-manager\src\test\java\com\personal\finance\manager"

# Create new package structure
$packages = @("auth", "user", "transaction", "category", "goal", "report", "security", "common", "config", "exception")
foreach ($pkg in $packages) {
    New-Item -ItemType Directory -Force -Path "$src\$pkg"
    New-Item -ItemType Directory -Force -Path "$src\$pkg\controller"
    New-Item -ItemType Directory -Force -Path "$src\$pkg\service"
    New-Item -ItemType Directory -Force -Path "$src\$pkg\repository"
    New-Item -ItemType Directory -Force -Path "$src\$pkg\dto"
    New-Item -ItemType Directory -Force -Path "$src\$pkg\entity"
    New-Item -ItemType Directory -Force -Path "$src\$pkg\config"
}

# Move Auth
Move-Item -Path "$src\controller\AuthController.java" -Destination "$src\auth\controller\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\service\AuthService.java" -Destination "$src\auth\service\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\LoginRequest.java" -Destination "$src\auth\dto\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\UserRegistrationRequest.java" -Destination "$src\auth\dto\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\UserLoginRequest.java" -Destination "$src\auth\dto\" -ErrorAction SilentlyContinue

# Move User
Move-Item -Path "$src\entity\User.java" -Destination "$src\user\entity\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\repository\UserRepository.java" -Destination "$src\user\repository\" -ErrorAction SilentlyContinue

# Move Transaction
Move-Item -Path "$src\controller\TransactionController.java" -Destination "$src\transaction\controller\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\service\TransactionService.java" -Destination "$src\transaction\service\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\repository\TransactionRepository.java" -Destination "$src\transaction\repository\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\entity\Transaction.java" -Destination "$src\transaction\entity\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\entity\TransactionType.java" -Destination "$src\transaction\entity\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\TransactionRequest.java" -Destination "$src\transaction\dto\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\TransactionResponse.java" -Destination "$src\transaction\dto\" -ErrorAction SilentlyContinue

# Move Category
Move-Item -Path "$src\controller\CategoryController.java" -Destination "$src\category\controller\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\service\CategoryService.java" -Destination "$src\category\service\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\repository\CategoryRepository.java" -Destination "$src\category\repository\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\entity\Category.java" -Destination "$src\category\entity\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\CategoryRequest.java" -Destination "$src\category\dto\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\CategoryResponse.java" -Destination "$src\category\dto\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\CategorySumDTO.java" -Destination "$src\category\dto\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\config\CategorySeeder.java" -Destination "$src\category\config\" -ErrorAction SilentlyContinue

# Move Goal
Move-Item -Path "$src\controller\GoalController.java" -Destination "$src\goal\controller\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\service\GoalService.java" -Destination "$src\goal\service\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\repository\GoalRepository.java" -Destination "$src\goal\repository\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\entity\Goal.java" -Destination "$src\goal\entity\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\GoalRequest.java" -Destination "$src\goal\dto\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\GoalResponse.java" -Destination "$src\goal\dto\" -ErrorAction SilentlyContinue

# Move Report
Move-Item -Path "$src\controller\ReportController.java" -Destination "$src\report\controller\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\service\ReportService.java" -Destination "$src\report\service\" -ErrorAction SilentlyContinue
Move-Item -Path "$src\dto\ReportResponse.java" -Destination "$src\report\dto\" -ErrorAction SilentlyContinue

# Move Common
Move-Item -Path "$src\dto\ApiResponse.java" -Destination "$src\common\dto\" -ErrorAction SilentlyContinue

# Security & Exception are already correctly named but let's make sure they don't have sub-layers unless needed.
# Let's keep them in their own packages (which exist), no need to move files, just ensure they are fine.

# Now process TEST files
foreach ($pkg in $packages) {
    New-Item -ItemType Directory -Force -Path "$testSrc\$pkg"
    New-Item -ItemType Directory -Force -Path "$testSrc\$pkg\controller"
    New-Item -ItemType Directory -Force -Path "$testSrc\$pkg\service"
    New-Item -ItemType Directory -Force -Path "$testSrc\$pkg\repository"
}

Move-Item -Path "$testSrc\controller\AuthControllerTest.java" -Destination "$testSrc\auth\controller\" -ErrorAction SilentlyContinue
Move-Item -Path "$testSrc\controller\CategoryControllerTest.java" -Destination "$testSrc\category\controller\" -ErrorAction SilentlyContinue
Move-Item -Path "$testSrc\service\CategoryServiceTest.java" -Destination "$testSrc\category\service\" -ErrorAction SilentlyContinue
Move-Item -Path "$testSrc\controller\GoalControllerTest.java" -Destination "$testSrc\goal\controller\" -ErrorAction SilentlyContinue
Move-Item -Path "$testSrc\service\GoalServiceTest.java" -Destination "$testSrc\goal\service\" -ErrorAction SilentlyContinue
Move-Item -Path "$testSrc\controller\TransactionControllerTest.java" -Destination "$testSrc\transaction\controller\" -ErrorAction SilentlyContinue
Move-Item -Path "$testSrc\service\TransactionServiceTest.java" -Destination "$testSrc\transaction\service\" -ErrorAction SilentlyContinue

Write-Host "Files moved successfully."
