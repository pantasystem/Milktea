import { z } from "zod";

const AccountSchema = z.object({
  id: z.string(),
  createdAt: z.date(),
  updatedAt: z.date(),
});

type Account = z.infer<typeof AccountSchema>;

export default Account
export {AccountSchema};