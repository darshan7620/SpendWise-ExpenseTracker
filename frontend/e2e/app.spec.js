import { test, expect } from '@playwright/test'

const API = 'http://localhost:8080/api/v1'

function uniqueEmail() {
  return `e2e+${Date.now()}${Math.floor(Math.random() * 1000)}@example.com`
}

test('unauthenticated access to a protected route redirects to login', async ({ page }) => {
  await page.goto('/dashboard')
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: 'Welcome back' })).toBeVisible()
})

test('unknown route renders the not-found page', async ({ page }) => {
  await page.goto('/definitely-not-a-real-page')
  await expect(page.getByRole('heading', { name: 'Page not found' })).toBeVisible()
})

test('register, create a category and a transaction, then sign out', async ({ page }) => {
  const email = uniqueEmail()

  await page.goto('/register')
  await page.fill('#reg-name', 'E2E User')
  await page.fill('#reg-email', email)
  await page.fill('#reg-password', 'secret123')
  await page.fill('#reg-confirm', 'secret123')
  await page.getByRole('button', { name: 'Create account' }).click()

  await expect(page).toHaveURL(/\/dashboard$/)
  await expect(page.getByText('E2E User')).toBeVisible()

  await page.getByRole('link', { name: 'Categories' }).click()
  await expect(page).toHaveURL(/\/categories$/)
  await page.getByRole('button', { name: 'Add category' }).first().click()
  await page.fill('#cat-name', 'E2E Groceries')
  await page.getByRole('button', { name: 'Save category' }).click()
  await expect(page.getByText('E2E Groceries')).toBeVisible()

  await page.getByRole('link', { name: 'Transactions' }).click()
  await expect(page).toHaveURL(/\/transactions$/)
  await page.getByRole('button', { name: 'Add transaction' }).first().click()
  await page.fill('#tx-title', 'E2E Coffee')
  await page.fill('#tx-amount', '120')
  await page.getByRole('button', { name: 'Save transaction' }).click()
  await expect(page.getByText('E2E Coffee')).toBeVisible()

  await page.getByRole('button', { name: 'Sign out' }).first().click()
  await expect(page).toHaveURL(/\/login$/)
})

test('login with the wrong password surfaces an error', async ({ page, request }) => {
  const email = uniqueEmail()
  const res = await request.post(`${API}/auth/register`, {
    data: { fullName: 'E2E Login', email, password: 'secret123' },
  })
  expect(res.ok()).toBeTruthy()

  await page.goto('/login')
  await page.fill('#login-email', email)
  await page.fill('#login-password', 'wrong-password')
  await page.getByRole('button', { name: 'Sign in' }).click()
  await expect(page.getByText(/invalid|incorrect|bad credentials/i)).toBeVisible()

  await page.fill('#login-password', 'secret123')
  await page.getByRole('button', { name: 'Sign in' }).click()
  await expect(page).toHaveURL(/\/dashboard$/)
})
